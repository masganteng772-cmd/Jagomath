#!/usr/bin/env python3
"""
EQ-Math 60Hz Showcase GIF Generator.

Renders 4 animated Material 3 Android UI showcase GIFs for README.md and GitHub Pages:
  1. 01-home-dashboard.gif       - Beranda, XP level progress, Star Milestones & Misi Harian
  2. 02-quiz-gameplay.gif        - Interactive Quiz, Bantuan Visual dot groups, Combo & Timer
  3. 03-rewards-celebration.gif  - Quiz Result Trophy, 3-Star Pop, Confetti & Badge Unlock
  4. 04-table-guide-mastery.gif  - Smart Table Guide (1-12) & Mastery Heatmap

All frames use official EQ-Math design tokens, bundled TTF fonts, vector icons,
and real app visual assets. Timeline physics run smoothly and encode to constant 50fps GIFs
(20ms frame delay avoids Chromium/Firefox/Safari 10ms -> 100ms legacy clamping).
"""

import concurrent.futures
import math
import os
import shutil
import subprocess
import tempfile
from pathlib import Path

ROOT = Path(__file__).resolve().parent.parent
OUT_DIR = ROOT / "docs" / "assets" / "showcase"
DOCS_ASSETS = ROOT / "docs" / "assets"

FONT_DISPLAY = str(ROOT / "app/src/main/res/font/fredoka.ttf")
FONT_BODY = str(ROOT / "app/src/main/res/font/plus_jakarta_sans.ttf")
LOGO_SRC = str(ROOT / "app/src/main/res/drawable/img_eq_math_logo.png")
TROPHY_SRC = str(ROOT / "app/src/main/res/drawable/img_trophy_celebration_1795454062869.jpg")
ICON_SRC = str(ROOT / "app/src/main/res/drawable/img_eqmath_icon_1795456333236.jpg")

W, H = 400, 720
SX0, SY0, SX1, SY1 = 20, 18, 380, 702  # Inner Android screen: 360 x 684
CX_IMG, CY_IMG = W / 2.0, H / 2.0

# Official EQ-Math Design Tokens (from Color.kt)
NAVY_500 = "#2D2C70"
NAVY_700 = "#1F1F4E"
NAVY_900 = "#12122D"
NAVY_100 = "#E6E6F2"

YELLOW_500 = "#F6B908"
YELLOW_600 = "#9E6E00"
YELLOW_700 = "#7D5A00"
YELLOW_100 = "#FEF4D6"
YELLOW_900 = "#4A3500"

RED_500 = "#E94639"
RED_600 = "#C42B20"
RED_100 = "#FDEAE8"
RED_900 = "#5C120C"

GREEN_500 = "#35A853"
GREEN_600 = "#1B7435"
GREEN_100 = "#E3F5E8"
GREEN_900 = "#0F421D"

BLUE_500 = "#4285F4"
BLUE_600 = "#1D5BC4"
BLUE_100 = "#E6F0FE"
BLUE_900 = "#102B5C"

CANVAS = "#EFF0F6"
SURFACE = "#FFFFFF"
SUBTLE = "#E8EAF3"
TRACK = "#DBDDE8"
BORDER = "#C8C9DC"
TEXT_PRIMARY = "#12122D"
TEXT_MUTED = "#4A4966"

FPS = 50  # 20ms per frame (max constant browser-safe GIF playback without 100ms clamp)
DURATION_SEC = 3.6
TOTAL_FRAMES = int(FPS * DURATION_SEC)  # 180 frames per GIF


def clamp(v: float, lo: float = 0.0, hi: float = 1.0) -> float:
    return max(lo, min(hi, v))


def ease_out_cubic(t: float) -> float:
    t = clamp(t)
    return 1.0 - (1.0 - t) ** 3


def ease_out_back(t: float, s: float = 1.7) -> float:
    t = clamp(t)
    u = t - 1.0
    return 1.0 + (s + 1.0) * (u ** 3) + s * (u ** 2)


def seg(t: float, a: float, b: float) -> float:
    if b <= a:
        return 1.0 if t >= b else 0.0
    return clamp((t - a) / (b - a))


def esc(text: str) -> str:
    return text.replace("\\", "\\\\").replace('"', '\\"')


def star_points(cx: float, cy: float, r_outer: float, r_inner: float) -> str:
    pts = []
    for i in range(10):
        angle = -math.pi / 2.0 + i * math.pi / 5.0
        r = r_outer if i % 2 == 0 else r_inner
        pts.append(f"{cx + r * math.cos(angle):.1f},{cy + r * math.sin(angle):.1f}")
    return " ".join(pts)


class MvgBuilder:
    def __init__(self):
        self.lines: list[str] = []

    def raw(self, line: str):
        self.lines.append(line)

    def rect(self, x0, y0, x1, y1, r=0, fill="none", stroke="none", sw=1.0):
        self.lines.append(f'fill "{fill}" stroke "{stroke}" stroke-width {sw:.2f}')
        if r > 0:
            self.lines.append(f"roundrectangle {x0:.1f},{y0:.1f} {x1:.1f},{y1:.1f} {r:.1f},{r:.1f}")
        else:
            self.lines.append(f"rectangle {x0:.1f},{y0:.1f} {x1:.1f},{y1:.1f}")

    def circle(self, cx, cy, rad, fill="none", stroke="none", sw=1.0):
        if rad <= 0.2:
            return
        self.lines.append(f'fill "{fill}" stroke "{stroke}" stroke-width {sw:.2f}')
        self.lines.append(f"circle {cx:.1f},{cy:.1f} {cx + rad:.1f},{cy:.1f}")

    def line(self, x0, y0, x1, y1, stroke="#C8C9DC", sw=1.0):
        self.lines.append(f'fill "none" stroke "{stroke}" stroke-width {sw:.2f} stroke-linecap round')
        self.lines.append(f"line {x0:.1f},{y0:.1f} {x1:.1f},{y1:.1f}")

    def star(self, cx, cy, r_outer, fill=YELLOW_500, stroke="none", sw=1.0):
        if r_outer <= 0.5:
            return
        pts = star_points(cx, cy, r_outer, r_outer * 0.44)
        self.lines.append(f'fill "{fill}" stroke "{stroke}" stroke-width {sw:.2f}')
        self.lines.append(f"polygon {pts}")

    def icon_close(self, cx, cy, r=5.0, color=TEXT_PRIMARY, sw=2.2):
        self.line(cx - r, cy - r, cx + r, cy + r, stroke=color, sw=sw)
        self.line(cx + r, cy - r, cx - r, cy + r, stroke=color, sw=sw)

    def icon_check(self, cx, cy, s=5.5, color=SURFACE, sw=2.3):
        self.lines.append(f'fill "none" stroke "{color}" stroke-width {sw:.2f} stroke-linecap round stroke-linejoin round')
        self.lines.append(f"polyline {cx - s:.1f},{cy + 0.5:.1f} {cx - s * 0.25:.1f},{cy + s * 0.75:.1f} {cx + s:.1f},{cy - s * 0.75:.1f}")

    def icon_arrow_right(self, cx, cy, s=5.5, color=NAVY_700, sw=2.0):
        self.line(cx - s, cy, cx + s, cy, stroke=color, sw=sw)
        self.lines.append(f'fill "none" stroke "{color}" stroke-width {sw:.2f} stroke-linecap round stroke-linejoin round')
        self.lines.append(f"polyline {cx + 1.0:.1f},{cy - s * 0.75:.1f} {cx + s:.1f},{cy:.1f} {cx + 1.0:.1f},{cy + s * 0.75:.1f}")

    def icon_arrow_left(self, cx, cy, s=5.5, color=TEXT_PRIMARY, sw=2.0):
        self.line(cx - s, cy, cx + s, cy, stroke=color, sw=sw)
        self.lines.append(f'fill "none" stroke "{color}" stroke-width {sw:.2f} stroke-linecap round stroke-linejoin round')
        self.lines.append(f"polyline {cx - 1.0:.1f},{cy - s * 0.75:.1f} {cx - s:.1f},{cy:.1f} {cx - 1.0:.1f},{cy + s * 0.75:.1f}")

    def icon_play(self, cx, cy, s=5.5, color=SURFACE):
        self.lines.append(f'fill "{color}" stroke "none"')
        self.lines.append(f"polygon {cx - s * 0.7:.1f},{cy - s:.1f} {cx + s:.1f},{cy:.1f} {cx - s * 0.7:.1f},{cy + s:.1f}")

    def icon_clock(self, cx, cy, r=6.0, color=BLUE_600):
        self.circle(cx, cy, r, fill="none", stroke=color, sw=1.6)
        self.line(cx, cy, cx, cy - r * 0.55, stroke=color, sw=1.5)
        self.line(cx, cy, cx + r * 0.45, cy + r * 0.2, stroke=color, sw=1.5)

    def text_nw(self, x, y, txt, size=14, color=TEXT_PRIMARY, font=FONT_BODY):
        self.lines.append(f'stroke "none" fill "{color}" font "{font}" font-size {size:.1f}')
        self.lines.append(f'gravity NorthWest text {x:.1f},{y:.1f} "{esc(txt)}"')

    def text_center(self, cx, cy, txt, size=14, color=TEXT_PRIMARY, font=FONT_DISPLAY):
        dx = cx - CX_IMG
        dy = cy - CY_IMG
        self.lines.append(f'stroke "none" fill "{color}" font "{font}" font-size {size:.1f}')
        self.lines.append(f'gravity Center text {dx:.1f},{dy:.1f} "{esc(txt)}"')

    def text_ne(self, right_x, y, txt, size=14, color=TEXT_PRIMARY, font=FONT_BODY):
        dx = W - right_x
        self.lines.append(f'stroke "none" fill "{color}" font "{font}" font-size {size:.1f}')
        self.lines.append(f'gravity NorthEast text {dx:.1f},{y:.1f} "{esc(txt)}"')

    def image(self, x, y, w, h, path):
        self.lines.append(f'gravity NorthWest image over {x:.1f},{y:.1f} {w:.1f},{h:.1f} "{path}"')

    def touch_ripple(self, cx, cy, progress):
        if progress <= 0.0 or progress >= 1.0:
            return
        r = 8 + 26 * ease_out_cubic(progress)
        alpha = (1.0 - progress) * 0.45
        self.circle(cx, cy, r, fill=f"rgba(45,44,112,{alpha:.3f})", stroke=f"rgba(246,185,8,{alpha*1.4:.3f})", sw=2.0)
        dot_alpha = (1.0 - progress) * 0.75
        self.circle(cx, cy, 7 * (1.0 - 0.3 * progress), fill=f"rgba(246,185,8,{dot_alpha:.3f})")

    def build(self) -> str:
        return "\n".join(self.lines)


def draw_phone_shell(g: MvgBuilder, screen_bg: str = CANVAS):
    g.rect(0, 0, W, H, r=0, fill="#14142E")
    g.circle(45, 55, 110, fill="rgba(66,133,244,0.12)")
    g.circle(365, 665, 120, fill="rgba(246,185,8,0.10)")
    g.circle(355, 85, 85, fill="rgba(233,70,57,0.10)")

    g.rect(14, 12, 386, 708, r=32, fill="#090918", stroke="#383768", sw=1.8)
    g.rect(SX0, SY0, SX1, SY1, r=24, fill=screen_bg)

    g.text_nw(36, 25, "10:55", size=11, color=TEXT_PRIMARY, font=FONT_DISPLAY)
    g.rect(172, 24, 228, 34, r=5, fill="#16162E")
    g.circle(216, 29, 3, fill="#2E3A68")
    g.rect(344, 27, 362, 35, r=2, fill="none", stroke=TEXT_PRIMARY, sw=1.2)
    g.rect(346, 29, 358, 33, r=1, fill=GREEN_600)
    g.rect(363, 29, 365, 33, r=0, fill=TEXT_PRIMARY)
    for i, bh in enumerate([4, 6, 8]):
        bx = 326 + i * 5
        g.rect(bx, 35 - bh, bx + 3, 35, r=1, fill=TEXT_PRIMARY)


def draw_bottom_nav(g: MvgBuilder, active_idx: int):
    g.rect(SX0, 636, SX1, SY1, r=22, fill=SURFACE)
    g.rect(SX0, 636, SX1, 660, r=0, fill=SURFACE)
    g.line(SX0, 636, SX1, 636, stroke=BORDER, sw=1.0)
    tabs = ["Beranda", "Latihan", "Lencana", "Progres"]
    tab_w = (SX1 - SX0) / 4.0
    for i, label in enumerate(tabs):
        cx = SX0 + tab_w * (i + 0.5)
        if i == active_idx:
            g.rect(cx - 26, 644, cx + 26, 666, r=11, fill=NAVY_100)
            dot_c = NAVY_500
            txt_c = NAVY_500
        else:
            dot_c = TEXT_MUTED
            txt_c = TEXT_MUTED
        if i == 0:
            g.rect(cx - 6, 651, cx + 6, 660, r=2, fill=dot_c)
            g.raw(f'fill "{dot_c}" stroke "none" polygon {cx:.1f},646 {cx-8:.1f},653 {cx+8:.1f},653')
        elif i == 1:
            g.rect(cx - 6, 647, cx + 6, 661, r=2.5, fill="none", stroke=dot_c, sw=1.6)
            g.text_center(cx, 654, "×", size=11, color=dot_c, font=FONT_DISPLAY)
        elif i == 2:
            g.star(cx, 654, 7.5, fill=dot_c)
        else:
            g.rect(cx - 7, 655, cx - 3, 661, r=1, fill=dot_c)
            g.rect(cx - 2, 651, cx + 2, 661, r=1, fill=dot_c)
            g.rect(cx + 3, 647, cx + 7, 661, r=1, fill=dot_c)
        g.text_center(cx, 678, label, size=10, color=txt_c, font=FONT_DISPLAY if i == active_idx else FONT_BODY)

    g.rect(154, 692, 246, 696, r=2, fill="#C0C2D6")


def draw_4_quadrant_stripe(g: MvgBuilder, x0, y0, x1, h=5):
    seg_w = (x1 - x0) / 4.0
    for i, c in enumerate([YELLOW_500, RED_500, GREEN_500, BLUE_500]):
        g.rect(x0 + i * seg_w, y0, x0 + (i + 1) * seg_w, y0 + h, r=0, fill=c)


# ==============================================================================
# GIF 1: HOME DASHBOARD, XP PROGRESS, STAR MILESTONES & MISI HARIAN
# ==============================================================================
def build_frame_home(frame_idx: int, cache: dict) -> str:
    t = frame_idx / TOTAL_FRAMES
    g = MvgBuilder()
    draw_phone_shell(g, CANVAS)

    # Top bar: EQ-Math Logo Plate + Streak Pill
    g.rect(34, 48, 152, 82, r=11, fill=SURFACE, stroke=BORDER, sw=1.0)
    g.image(42, 53, 100, 24, cache["logo"])

    pulse = 1.0 + 0.06 * math.sin(t * 2 * math.pi * 2)
    g.rect(288, 50, 366, 80, r=11, fill=YELLOW_100, stroke=YELLOW_500, sw=1.1)
    g.circle(306, 65, 6.5 * pulse, fill=RED_500)
    g.circle(306, 67, 3.5 * pulse, fill=YELLOW_500)
    g.text_nw(319, 57, "5 hr", size=13, color=YELLOW_900, font=FONT_DISPLAY)

    # 1. Greeting & Level Card
    g.rect(34, 92, 366, 258, r=16, fill=SURFACE, stroke=BORDER, sw=1.0)
    draw_4_quadrant_stripe(g, 42, 93, 358, h=5)

    g.text_nw(48, 108, "Halo, Juara Cilik!", size=20, color=TEXT_PRIMARY, font=FONT_DISPLAY)
    g.text_nw(48, 134, "Belajar matematika jadi mudah & seru!", size=11, color=TEXT_MUTED, font=FONT_BODY)

    xp_anim = ease_out_cubic(seg(t, 0.05, 0.45))
    xp_val = int(420 + 260 * xp_anim)
    xp_frac = xp_val / 800.0

    g.rect(48, 157, 94, 177, r=7, fill=NAVY_500)
    g.text_center(71, 167, "Lv. 4", size=11, color=SURFACE, font=FONT_DISPLAY)
    g.text_nw(101, 159, "Pahlawan Perkalian", size=12.5, color=TEXT_PRIMARY, font=FONT_DISPLAY)
    g.text_ne(352, 160, f"{xp_val}/800 XP", size=11, color=NAVY_500, font=FONT_DISPLAY)

    g.rect(48, 184, 352, 194, r=5, fill=TRACK)
    g.rect(48, 184, 48 + 304 * xp_frac, 194, r=5, fill=NAVY_500)

    stats = [
        ("24", "Bintang", YELLOW_500),
        ("7/12", "Lencana", BLUE_500),
        ("14/24", "Tabel Mahir", GREEN_500),
    ]
    for i, (val, lbl, dot) in enumerate(stats):
        bx0 = 48 + i * 104
        bx1 = bx0 + 96
        g.rect(bx0, 204, bx1, 248, r=11, fill=SUBTLE, stroke=BORDER, sw=0.8)
        if i == 0:
            g.star(bx0 + 48, 214, 5.5, fill=dot)
        else:
            g.circle(bx0 + 48, 214, 4.5, fill=dot)
        g.text_center(bx0 + 48, 227, val, size=14, color=TEXT_PRIMARY, font=FONT_DISPLAY)
        g.text_center(bx0 + 48, 240, lbl, size=9.5, color=TEXT_MUTED, font=FONT_BODY)

    # 2. Reward Dashboard (Star Milestones)
    g.rect(34, 268, 366, 378, r=16, fill=SURFACE, stroke=BORDER, sw=1.0)
    g.text_nw(48, 280, "Papan Hadiah & Bintang", size=13.5, color=TEXT_PRIMARY, font=FONT_DISPLAY)
    g.text_ne(352, 281, "Bintang Perak", size=11, color=YELLOW_700, font=FONT_DISPLAY)

    ms_anim = ease_out_cubic(seg(t, 0.15, 0.60))
    ms_frac = 0.55 + 0.25 * ms_anim
    g.rect(48, 304, 352, 314, r=5, fill=TRACK)
    g.rect(48, 304, 48 + 304 * ms_frac, 314, r=5, fill=YELLOW_500)
    g.text_nw(48, 320, "Target: Bintang Emas (24/30)", size=10.5, color=TEXT_MUTED, font=FONT_BODY)
    g.text_ne(352, 320, "6 bintang lagi!", size=10.5, color=NAVY_500, font=FONT_DISPLAY)

    milestones = [("5", True), ("15", True), ("30", False), ("60", False), ("100", False)]
    for i, (mlbl, reached) in enumerate(milestones):
        mx = 48 + i * 62
        bg = YELLOW_100 if reached else SUBTLE
        bd = YELLOW_500 if reached else BORDER
        tc = YELLOW_900 if reached else TEXT_MUTED
        sc = YELLOW_600 if reached else BORDER
        g.rect(mx, 340, mx + 56, 366, r=10, fill=bg, stroke=bd, sw=1.0)
        g.star(mx + 14, 353, 5.2, fill=sc)
        g.text_center(mx + 35, 353, mlbl, size=11.5, color=tc, font=FONT_DISPLAY)

    # 3. Daily Challenge Deep Navy Hero Banner (seamless navy + soft accent circle)
    g.rect(34, 388, 366, 526, r=18, fill=NAVY_500)
    g.circle(340, 424, 34, fill="rgba(255,255,255,0.06)")
    g.circle(324, 496, 46, fill="rgba(246,185,8,0.08)")
    sweep_x = 10 + 420 * seg(t, 0.25, 0.75)
    if 46 < sweep_x < 354:
        g.rect(max(36, sweep_x - 20), 392, min(364, sweep_x + 20), 522, r=8, fill="rgba(255,255,255,0.05)")

    g.rect(50, 402, 160, 424, r=11, fill=YELLOW_500)
    g.text_center(105, 413, "+30 BONUS XP", size=10.5, color=NAVY_900, font=FONT_DISPLAY)
    g.text_nw(50, 432, "Misi Harian", size=22, color=SURFACE, font=FONT_DISPLAY)
    g.text_nw(50, 460, "10 soal campuran perkalian & pembagian!", size=11, color="#D8D9F0", font=FONT_BODY)

    press = seg(t, 0.68, 0.88)
    btn_dy = 2.0 * math.sin(press * math.pi) if 0 < press < 1 else 0.0
    g.rect(50, 482 + btn_dy, 198, 514 + btn_dy, r=11, fill=SURFACE)
    g.text_center(114, 498 + btn_dy, "Mulai Misi", size=13, color=NAVY_700, font=FONT_DISPLAY)
    g.icon_arrow_right(168, 498 + btn_dy, s=5.5, color=NAVY_700, sw=2.1)

    # 4. Adventure Mode Cards Preview
    g.text_nw(36, 537, "Pilih Petualangan Belajar", size=13.5, color=TEXT_PRIMARY, font=FONT_DISPLAY)

    g.rect(34, 560, 194, 626, r=14, fill=SURFACE, stroke=BORDER, sw=1.0)
    g.rect(44, 570, 86, 616, r=11, fill=RED_600)
    g.text_center(65, 593, "×", size=26, color=SURFACE, font=FONT_DISPLAY)
    g.text_nw(94, 576, "Perkalian", size=13, color=TEXT_PRIMARY, font=FONT_DISPLAY)
    g.text_nw(94, 596, "Tabel 1 - 12", size=10, color=TEXT_MUTED, font=FONT_BODY)

    g.rect(206, 560, 366, 626, r=14, fill=SURFACE, stroke=BORDER, sw=1.0)
    g.rect(216, 570, 258, 616, r=11, fill=BLUE_600)
    g.text_center(237, 593, "÷", size=26, color=SURFACE, font=FONT_DISPLAY)
    g.text_nw(266, 576, "Pembagian", size=13, color=TEXT_PRIMARY, font=FONT_DISPLAY)
    g.text_nw(266, 596, "Bagi rata", size=10, color=TEXT_MUTED, font=FONT_BODY)

    if 0.68 <= t <= 0.92:
        g.touch_ripple(124, 498, seg(t, 0.68, 0.92))

    draw_bottom_nav(g, active_idx=0)
    return g.build()


# ==============================================================================
# GIF 2: INTERACTIVE QUIZ + BANTUAN VISUAL + COMBO & FEEDBACK
# ==============================================================================
def build_frame_quiz(frame_idx: int, cache: dict) -> str:
    t = frame_idx / TOTAL_FRAMES
    g = MvgBuilder()
    draw_phone_shell(g, CANVAS)

    hint_open = ease_out_cubic(seg(t, 0.15, 0.30))
    answered = t >= 0.52
    correct_anim = ease_out_cubic(seg(t, 0.52, 0.65))
    combo_val = 4 if answered else 3
    correct_cnt = 4 if answered else 3
    prog_val = 0.3 + 0.1 * correct_anim
    timer_sec = max(38, 42 - int(t * 4.0))

    # Top HUD Bar
    g.circle(48, 68, 16, fill=SURFACE, stroke=BORDER, sw=1.0)
    g.icon_close(48, 68, r=5.0, color=TEXT_PRIMARY, sw=2.1)

    g.text_nw(74, 53, "Soal 4 / 10", size=12.5, color=TEXT_PRIMARY, font=FONT_DISPLAY)
    g.text_ne(364, 53, f"Benar: {correct_cnt}", size=12, color=GREEN_600, font=FONT_DISPLAY)
    g.rect(74, 73, 364, 83, r=5, fill=TRACK)
    g.rect(74, 73, 74 + 290 * prog_val, 83, r=5, fill=NAVY_500)

    # Combo + Timer + Mode Strip
    combo_scale = 1.0 + 0.10 * math.sin(seg(t, 0.52, 0.70) * math.pi) if answered else 1.0
    g.rect(34, 96, 146, 126, r=15, fill=YELLOW_100, stroke=YELLOW_500, sw=1.2)
    g.circle(52, 111, 6 * combo_scale, fill=RED_500)
    g.circle(52, 113, 3.2 * combo_scale, fill=YELLOW_500)
    g.text_nw(64, 104, f"Kombo x{combo_val}", size=12.5, color=YELLOW_900, font=FONT_DISPLAY)

    g.rect(194, 96, 282, 126, r=15, fill=BLUE_100, stroke=BLUE_500, sw=1.0)
    g.icon_clock(212, 111, r=5.8, color=BLUE_600)
    g.text_nw(224, 104, f"{timer_sec} dtk", size=11.5, color=BLUE_900, font=FONT_DISPLAY)

    g.rect(290, 96, 366, 126, r=15, fill=NAVY_100)
    g.text_center(328, 111, "Mode Ketik", size=11, color=NAVY_500, font=FONT_DISPLAY)

    # Equation Card
    hint_h = 104 * hint_open
    card_bottom = 266 + hint_h
    g.rect(34, 138, 366, card_bottom, r=20, fill=SURFACE, stroke=BORDER, sw=1.1)

    g.rect(144, 152, 256, 174, r=11, fill=NAVY_100)
    g.text_center(200, 163, "Perkalian (×)", size=11.5, color=NAVY_500, font=FONT_DISPLAY)

    g.text_center(200, 208, "4 × 6 = ?", size=40, color=TEXT_PRIMARY, font=FONT_DISPLAY)

    hint_lbl = "Sembunyikan Bantuan" if hint_open > 0.5 else "Bantuan Visual"
    g.star(126 if hint_open > 0.5 else 148, 246, 5.5, fill=YELLOW_500)
    g.text_center(208, 246, hint_lbl, size=12, color=YELLOW_600, font=FONT_DISPLAY)

    # Visual Math Hint Box (4 groups x 6 red dots)
    if hint_open > 0.15:
        hy0 = 264
        hy1 = 264 + 92 * hint_open
        g.rect(48, hy0, 352, hy1, r=14, fill=YELLOW_100, stroke=YELLOW_500, sw=1.0)
        if hint_open > 0.55:
            g.text_center(200, hy0 + 18, "4 kelompok berisi 6 titik (4 × 6)", size=11, color=YELLOW_900, font=FONT_DISPLAY)
            dot_reveal = ease_out_cubic(seg(t, 0.24, 0.44))
            for grp in range(4):
                gx = 60 + (grp % 2) * 144
                gy = hy0 + 32 + (grp // 2) * 28
                g.rect(gx, gy, gx + 132, gy + 22, r=8, fill=SURFACE, stroke=BORDER, sw=0.8)
                for d in range(6):
                    if dot_reveal >= (grp * 6 + d) / 24.0:
                        g.circle(gx + 16 + d * 20, gy + 11, 5.2, fill=RED_600)

    # Answer Feedback Banner
    grid_y = card_bottom + 14
    if answered:
        fb_h = 54 * correct_anim
        g.rect(34, grid_y, 366, grid_y + fb_h, r=14, fill=GREEN_100, stroke=GREEN_600, sw=1.2)
        if correct_anim > 0.4:
            g.circle(60, grid_y + fb_h / 2.0, 12, fill=GREEN_600)
            g.icon_check(60, grid_y + fb_h / 2.0, s=5.5, color=SURFACE, sw=2.3)
            g.text_nw(82, grid_y + 10, "Mantap! Pertahankan terus!", size=13, color=GREEN_900, font=FONT_DISPLAY)
            g.text_nw(82, grid_y + 29, "+10 XP  •  Kombo Bonus +12 XP", size=10.5, color=GREEN_600, font=FONT_BODY)
        grid_y += fb_h + 12

    # 2x2 Multiple Choice Grid
    choices = [18, 24, 28, 20]
    btn_h = 66
    for idx, val in enumerate(choices):
        r = idx // 2
        c = idx % 2
        bx0 = 34 + c * 172
        by0 = grid_y + r * (btn_h + 10)
        bx1 = bx0 + 160
        by1 = by0 + btn_h
        bcx = (bx0 + bx1) / 2.0
        bcy = (by0 + by1) / 2.0

        if answered and val == 24:
            bg, fg, bd = GREEN_600, SURFACE, GREEN_600
        elif answered:
            bg, fg, bd = SUBTLE, TEXT_MUTED, BORDER
        else:
            bg, fg, bd = SURFACE, TEXT_PRIMARY, "#6D6C8A"

        g.rect(bx0, by0, bx1, by1, r=15, fill=bg, stroke=bd, sw=1.5)
        if answered and val == 24:
            g.circle(bcx - 28, bcy, 11, fill="rgba(255,255,255,0.22)")
            g.icon_check(bcx - 28, bcy, s=5.0, color=SURFACE, sw=2.2)
            g.text_center(bcx + 12, bcy, str(val), size=26, color=fg, font=FONT_DISPLAY)
        else:
            g.text_center(bcx, bcy, str(val), size=26, color=fg, font=FONT_DISPLAY)

    if answered:
        cta_y = grid_y + 2 * (btn_h + 10) + 2
        if cta_y + 48 <= SY1 - 12:
            g.rect(34, cta_y, 366, cta_y + 46, r=13, fill=BLUE_600)
            g.text_center(190, cta_y + 23, "Soal Berikutnya", size=14.5, color=SURFACE, font=FONT_DISPLAY)
            g.icon_arrow_right(262, cta_y + 23, s=5.5, color=SURFACE, sw=2.1)

    if 0.10 <= t <= 0.24:
        g.touch_ripple(200, 246, seg(t, 0.10, 0.24))
    if 0.46 <= t <= 0.62:
        g.touch_ripple(286, card_bottom + 14 + 33, seg(t, 0.46, 0.62))

    g.rect(154, 692, 246, 696, r=2, fill="#C0C2D6")
    return g.build()


# ==============================================================================
# GIF 3: QUIZ RESULT CELEBRATION, STARS, CONFETTI & BADGE UNLOCK
# ==============================================================================
def build_frame_celebration(frame_idx: int, cache: dict) -> str:
    t = frame_idx / TOTAL_FRAMES
    g = MvgBuilder()
    draw_phone_shell(g, CANVAS)

    # Confetti positioned cleanly in left/right celebration margins (never overlaps headline text)
    confetti_colors = [YELLOW_500, RED_500, GREEN_500, BLUE_500]
    for i in range(16):
        is_left = (i % 2 == 0)
        base_x = (36 + (i * 19) % 82) if is_left else (284 + (i * 17) % 78)
        cx = base_x + 8 * math.sin(t * 2 * math.pi + i)
        cy = 50 + ((i * 47 + int(t * 200)) % 220)
        col = confetti_colors[i % 4]
        sz = 4.0 + (i % 3) * 1.4
        if i % 3 == 0:
            g.circle(cx, cy, sz * 0.72, fill=col)
        else:
            g.rect(cx - sz, cy - sz * 0.48, cx + sz, cy + sz * 0.48, r=1, fill=col)

    trophy_bob = 3.0 * math.sin(t * 2 * math.pi * 2)
    tx0, ty0 = 136, 54 + trophy_bob
    g.rect(tx0 - 4, ty0 - 4, tx0 + 132, ty0 + 132, r=22, fill=SURFACE, stroke=BORDER, sw=1.2)
    g.image(tx0, ty0, 128, 128, cache["trophy"])

    star_specs = [
        (146, 218, 18.0, 0.08, 0.24),
        (200, 210, 24.0, 0.18, 0.36),
        (254, 218, 18.0, 0.28, 0.46),
    ]
    for scx, scy, r_max, a, b in star_specs:
        p = seg(t, a, b)
        if p > 0:
            scale = ease_out_back(p, s=2.2)
            g.star(scx, scy, r_max * scale, fill=YELLOW_500, stroke=YELLOW_600, sw=1.0)
        else:
            g.star(scx, scy, r_max * 0.85, fill=TRACK, stroke=BORDER, sw=1.0)

    g.text_center(200, 256, "Luar Biasa!", size=27, color=TEXT_PRIMARY, font=FONT_DISPLAY)
    g.text_center(200, 282, "Benar 10 dari 10 soal (100%)!", size=12.5, color=TEXT_MUTED, font=FONT_BODY)

    lvl_p = ease_out_cubic(seg(t, 0.22, 0.42))
    lvl_y = 298 + (1.0 - lvl_p) * 14
    g.rect(34, lvl_y, 366, lvl_y + 52, r=14, fill=GREEN_100, stroke=GREEN_600, sw=1.1)
    g.circle(62, lvl_y + 26, 15, fill=GREEN_600)
    g.star(62, lvl_y + 26, 8.5, fill=SURFACE)
    g.text_nw(88, lvl_y + 9, "Naik ke Level 5!", size=14, color=GREEN_900, font=FONT_DISPLAY)
    g.text_nw(88, lvl_y + 29, "Profesor Cilik • Selamat!", size=11, color=GREEN_600, font=FONT_BODY)

    cnt_p = ease_out_cubic(seg(t, 0.12, 0.55))
    acc_v = int(100 * cnt_p)
    cmb_v = int(10 * cnt_p)
    xp_v = int(285 * cnt_p)

    stat_cards = [
        (f"{acc_v}%", "Akurasi", GREEN_600),
        (f"x{cmb_v}", "Kombo", RED_600),
        (f"+{xp_v}", "Total XP", BLUE_600),
    ]
    for i, (val, lbl, accent) in enumerate(stat_cards):
        sx0 = 34 + i * 114
        sx1 = sx0 + 104
        g.rect(sx0, 362, sx1, 434, r=14, fill=SURFACE, stroke=BORDER, sw=1.0)
        g.circle(sx0 + 52, 378, 7, fill=accent)
        g.text_center(sx0 + 52, 400, val, size=18, color=TEXT_PRIMARY, font=FONT_DISPLAY)
        g.text_center(sx0 + 52, 420, lbl, size=10, color=TEXT_MUTED, font=FONT_BODY)

    bdg_p = ease_out_back(seg(t, 0.36, 0.62), s=1.2)
    bdg_off = (1.0 - min(1.0, bdg_p)) * 30
    by0 = 446 + bdg_off
    g.rect(34, by0, 366, by0 + 114, r=16, fill=YELLOW_100, stroke=YELLOW_500, sw=1.3)
    g.star(56, by0 + 21, 7.5, fill=YELLOW_600)
    g.text_nw(70, by0 + 12, "Lencana Baru Terbuka!", size=13.5, color=YELLOW_900, font=FONT_DISPLAY)

    g.rect(46, by0 + 40, 354, by0 + 102, r=12, fill=SURFACE, stroke=BORDER, sw=0.9)
    g.circle(74, by0 + 71, 18, fill=YELLOW_100, stroke=YELLOW_500, sw=1.2)
    g.star(74, by0 + 71, 10, fill=YELLOW_600)
    g.text_nw(102, by0 + 52, "Nilai Sempurna 100!", size=13, color=TEXT_PRIMARY, font=FONT_DISPLAY)
    g.text_nw(102, by0 + 72, "Selesaikan kuis tanpa salah", size=10.5, color=TEXT_MUTED, font=FONT_BODY)
    g.text_ne(344, by0 + 62, "+150 XP", size=11.5, color=YELLOW_600, font=FONT_DISPLAY)

    g.rect(34, 578, 366, 624, r=13, fill=NAVY_500)
    g.icon_play(156, 601, s=5.2, color=SURFACE)
    g.text_center(206, 601, "Main Lagi", size=14.5, color=SURFACE, font=FONT_DISPLAY)

    g.rect(34, 634, 366, 678, r=13, fill=SURFACE, stroke="#6D6C8A", sw=1.3)
    g.text_center(200, 656, "Kembali ke Beranda", size=13.5, color=NAVY_500, font=FONT_DISPLAY)

    if 0.72 <= t <= 0.92:
        g.touch_ripple(200, 601, seg(t, 0.72, 0.92))

    g.rect(154, 692, 246, 696, r=2, fill="#C0C2D6")
    return g.build()


# ==============================================================================
# GIF 4: SMART TABLE GUIDE (1-12) & MASTERY HEATMAP
# ==============================================================================
def build_frame_table_guide(frame_idx: int, cache: dict) -> str:
    t = frame_idx / TOTAL_FRAMES
    g = MvgBuilder()
    draw_phone_shell(g, CANVAS)

    switched = t >= 0.48
    sel_table = 8 if switched else 7

    g.circle(48, 66, 16, fill=SURFACE, stroke=BORDER, sw=1.0)
    g.icon_arrow_left(48, 66, s=5.5, color=TEXT_PRIMARY, sw=2.1)
    g.text_nw(74, 54, "Tabel Pintar SD", size=19, color=TEXT_PRIMARY, font=FONT_DISPLAY)

    g.rect(34, 92, 164, 124, r=12, fill=NAVY_100, stroke=NAVY_500, sw=1.2)
    g.icon_check(52, 108, s=4.5, color=NAVY_500, sw=2.1)
    g.text_nw(64, 100, "Perkalian (×)", size=12, color=NAVY_500, font=FONT_DISPLAY)

    g.rect(174, 92, 304, 124, r=12, fill=SURFACE, stroke=BORDER, sw=1.0)
    g.text_center(239, 108, "Pembagian (÷)", size=12, color=TEXT_MUTED, font=FONT_BODY)

    nums = [5, 6, 7, 8, 9, 10]
    for i, n in enumerate(nums):
        nx0 = 34 + i * 56
        nx1 = nx0 + 48
        is_sel = n == sel_table
        g.rect(nx0, 134, nx1, 178, r=12, fill=NAVY_500 if is_sel else SURFACE, stroke="none" if is_sel else BORDER, sw=1.0)
        g.text_center(nx0 + 24, 156, str(n), size=16, color=SURFACE if is_sel else TEXT_PRIMARY, font=FONT_DISPLAY)

    cascade = ease_out_cubic(seg(t, 0.48, 0.78)) if switched else ease_out_cubic(seg(t, 0.02, 0.34))
    for factor in range(1, 7):
        ry0 = 190 + (factor - 1) * 43
        ry1 = ry0 + 36
        bg = SURFACE if factor % 2 == 0 else SUBTLE
        hi = abs(cascade * 6.0 - (factor - 0.5)) < 0.75
        bd = NAVY_500 if hi else BORDER
        g.rect(34, ry0, 366, ry1, r=11, fill=bg, stroke=bd, sw=1.4 if hi else 0.9)
        g.text_nw(52, ry0 + 9, f"{sel_table} × {factor}", size=16, color=TEXT_PRIMARY, font=FONT_DISPLAY)
        g.rect(284, ry0 + 5, 354, ry1 - 5, r=8, fill=NAVY_100)
        g.text_center(319, (ry0 + ry1) / 2.0, f"= {sel_table * factor}", size=15, color=NAVY_500, font=FONT_DISPLAY)

    g.rect(34, 456, 366, 616, r=16, fill=SURFACE, stroke=BORDER, sw=1.0)
    g.text_nw(48, 468, "Peta Penguasaan Tabel (1–12)", size=13, color=TEXT_PRIMARY, font=FONT_DISPLAY)
    g.text_ne(316, 470, "Mahir", size=10.5, color=GREEN_600, font=FONT_DISPLAY)
    for s in range(3):
        g.star(326 + s * 11, 476, 4.5, fill=YELLOW_500)

    mastery_stars = [3, 3, 3, 3, 3, 2, 3, 3 if switched else 2, 2, 3, 1, 2]
    for idx in range(12):
        r = idx // 6
        c = idx % 6
        tx0 = 48 + c * 52
        ty0 = 496 + r * 56
        tx1 = tx0 + 46
        ty1 = ty0 + 48
        stars = mastery_stars[idx]
        is_active = (idx + 1) == sel_table
        if stars == 3:
            bg, tc, bd = GREEN_100, GREEN_900, GREEN_600
        elif stars == 2:
            bg, tc, bd = BLUE_100, BLUE_900, BLUE_500
        else:
            bg, tc, bd = YELLOW_100, YELLOW_900, YELLOW_500
        if is_active:
            bd = NAVY_500
        g.rect(tx0, ty0, tx1, ty1, r=10, fill=bg, stroke=bd, sw=2.0 if is_active else 1.0)
        g.text_center(tx0 + 23, ty0 + 16, str(idx + 1), size=14, color=tc, font=FONT_DISPLAY)
        for s in range(3):
            sc = YELLOW_600 if s < stars else BORDER
            g.star(tx0 + 12 + s * 11, ty0 + 36, 4.0, fill=sc)

    pulse = 1.0 + 0.02 * math.sin(t * 2 * math.pi * 2)
    g.rect(34, 628, 366, 676, r=13, fill=NAVY_500)
    g.icon_play(122, 652, s=5.5, color=SURFACE)
    g.text_center(208, 652, f"Latih Tabel Ini (×{sel_table})", size=14.5 * pulse, color=SURFACE, font=FONT_DISPLAY)

    if 0.42 <= t <= 0.56:
        g.touch_ripple(34 + 3 * 56 + 24, 156, seg(t, 0.42, 0.56))

    g.rect(154, 692, 246, 696, r=2, fill="#C0C2D6")
    return g.build()


def render_single_frame(args):
    mvg_text, out_png = args
    with tempfile.NamedTemporaryFile("w", suffix=".mvg", delete=False) as f:
        f.write(mvg_text)
        mvg_path = f.name
    try:
        subprocess.run(
            [
                "convert",
                "-size",
                f"{W}x{H}",
                "xc:#14142E",
                "-antialias",
                "-draw",
                f"@{mvg_path}",
                f"PNG24:{out_png}",
            ],
            check=True,
            stdout=subprocess.DEVNULL,
            stderr=subprocess.DEVNULL,
        )
    finally:
        os.unlink(mvg_path)


def encode_gif(frames_dir: Path, out_gif: Path):
    palette = frames_dir / "palette.png"
    pattern = str(frames_dir / "frame_%04d.png")
    subprocess.run(
        [
            "ffmpeg",
            "-y",
            "-framerate",
            str(FPS),
            "-i",
            pattern,
            "-vf",
            "palettegen=max_colors=128:stats_mode=diff",
            str(palette),
        ],
        check=True,
        stdout=subprocess.DEVNULL,
        stderr=subprocess.DEVNULL,
    )
    subprocess.run(
        [
            "ffmpeg",
            "-y",
            "-framerate",
            str(FPS),
            "-i",
            pattern,
            "-i",
            str(palette),
            "-lavfi",
            f"fps={FPS}[x];[x][1:v]paletteuse=dither=bayer:bayer_scale=3:diff_mode=rectangle",
            "-loop",
            "0",
            str(out_gif),
        ],
        check=True,
        stdout=subprocess.DEVNULL,
        stderr=subprocess.DEVNULL,
    )


def prepare_cache(tmp_root: Path) -> dict:
    logo_out = tmp_root / "logo.png"
    trophy_out = tmp_root / "trophy.png"

    subprocess.run(
        ["convert", LOGO_SRC, "-resize", "100x24", str(logo_out)],
        check=True,
    )
    subprocess.run(
        [
            "convert",
            TROPHY_SRC,
            "-resize",
            "128x128^",
            "-gravity",
            "center",
            "-extent",
            "128x128",
            "(",
            "+clone",
            "-alpha",
            "extract",
            "-draw",
            "fill black polygon 0,0 0,18 18,0 fill white circle 18,18 18,0",
            "(",
            "+clone",
            "-flip",
            ")",
            "-compose",
            "Multiply",
            "-composite",
            "(",
            "+clone",
            "-flop",
            ")",
            "-compose",
            "Multiply",
            "-composite",
            ")",
            "-alpha",
            "off",
            "-compose",
            "CopyOpacity",
            "-composite",
            str(trophy_out),
        ],
        check=True,
    )
    return {"logo": str(logo_out), "trophy": str(trophy_out)}


def copy_static_docs_assets():
    DOCS_ASSETS.mkdir(parents=True, exist_ok=True)
    OUT_DIR.mkdir(parents=True, exist_ok=True)
    shutil.copy2(LOGO_SRC, DOCS_ASSETS / "logo.png")
    shutil.copy2(ICON_SRC, DOCS_ASSETS / "app-icon.jpg")
    shutil.copy2(TROPHY_SRC, DOCS_ASSETS / "trophy.jpg")


def main():
    copy_static_docs_assets()
    scenes = [
        ("01-home-dashboard.gif", build_frame_home),
        ("02-quiz-gameplay.gif", build_frame_quiz),
        ("03-rewards-celebration.gif", build_frame_celebration),
        ("04-table-guide-mastery.gif", build_frame_table_guide),
    ]

    workers = min(4, os.cpu_count() or 2)
    with tempfile.TemporaryDirectory(prefix="eqmath_gifs_") as tmp:
        tmp_root = Path(tmp)
        cache = prepare_cache(tmp_root)

        for filename, builder in scenes:
            scene_dir = tmp_root / filename.replace(".gif", "")
            scene_dir.mkdir(parents=True, exist_ok=True)
            tasks = [
                (builder(i, cache), str(scene_dir / f"frame_{i:04d}.png"))
                for i in range(TOTAL_FRAMES)
            ]
            with concurrent.futures.ProcessPoolExecutor(max_workers=workers) as pool:
                list(pool.map(render_single_frame, tasks))

            out_gif = OUT_DIR / filename
            encode_gif(scene_dir, out_gif)
            size_kb = out_gif.stat().st_size / 1024.0
            print(f"Generated {out_gif.relative_to(ROOT)} ({TOTAL_FRAMES} frames @ {FPS}fps, {size_kb:.1f} KB)")


if __name__ == "__main__":
    main()
