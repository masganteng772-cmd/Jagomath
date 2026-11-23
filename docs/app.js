(() => {
  // 1. Auto-detect GitHub repository URL when hosted on *.github.io/<repo>/
  const host = window.location.hostname;
  if (host.endsWith(".github.io")) {
    const owner = host.replace(".github.io", "");
    const segs = window.location.pathname.split("/").filter(Boolean);
    const repo = segs[0] || "EQ-Math";
    const repoUrl = `https://github.com/${owner}/${repo}`;
    const setHref = (id, url) => {
      const el = document.getElementById(id);
      if (el) el.href = url;
    };
    setHref("repoLinkTop", repoUrl);
    setHref("footerRepo", repoUrl);
    setHref("footerReleases", `${repoUrl}/releases`);
    setHref("footerIssues", `${repoUrl}/issues`);
  }

  // 2. Light / Dark Theme Toggle
  const html = document.documentElement;
  const themeBtn = document.getElementById("themeToggle");
  const savedTheme = localStorage.getItem("eqmath-theme");
  const prefersDark = window.matchMedia && window.matchMedia("(prefers-color-scheme: dark)").matches;
  const applyTheme = (theme) => {
    html.setAttribute("data-theme", theme);
    if (themeBtn) {
      themeBtn.querySelector(".theme-icon").textContent = theme === "dark" ? "☀️" : "🌙";
    }
  };
  applyTheme(savedTheme || (prefersDark ? "dark" : "light"));
  themeBtn?.addEventListener("click", () => {
    const next = html.getAttribute("data-theme") === "dark" ? "light" : "dark";
    localStorage.setItem("eqmath-theme", next);
    applyTheme(next);
  });

  // 3. Lightbox Modal for 2x2 Showcase GIFs
  const lightbox = document.getElementById("lightbox");
  const lbImg = document.getElementById("lightboxImg");
  const lbTitle = document.getElementById("lightboxTitle");
  const lbClose = document.getElementById("lightboxClose");

  const closeLightbox = () => {
    lightbox.hidden = true;
    lbImg.src = "";
  };

  document.querySelectorAll(".showcase-card").forEach((card) => {
    card.addEventListener("click", () => {
      lbImg.src = card.dataset.gif;
      lbImg.alt = card.dataset.title;
      lbTitle.textContent = card.dataset.title;
      lightbox.hidden = false;
    });
  });
  lbClose?.addEventListener("click", closeLightbox);
  lightbox?.querySelector(".lightbox-backdrop")?.addEventListener("click", closeLightbox);
  document.addEventListener("keydown", (e) => {
    if (e.key === "Escape" && !lightbox.hidden) closeLightbox();
  });

  // 4. Interactive Mini-Quiz Engine (mirrors QuizGenerator + Bantuan Visual)
  let selectedMode = "MUL";
  let combo = 0;
  let score = 0;
  let answered = false;
  let currentQ = null;

  const elCombo = document.getElementById("demoCombo");
  const elScore = document.getElementById("demoScore");
  const elBadge = document.getElementById("demoOpBadge");
  const elEq = document.getElementById("demoEquation");
  const elHintBtn = document.getElementById("demoHintBtn");
  const elHintPanel = document.getElementById("demoHintPanel");
  const elHintText = document.getElementById("demoHintText");
  const elDotGroups = document.getElementById("demoDotGroups");
  const elFeedback = document.getElementById("demoFeedback");
  const elChoices = document.getElementById("demoChoices");
  const elNextBtn = document.getElementById("demoNextBtn");

  const randInt = (min, max) => Math.floor(Math.random() * (max - min + 1)) + min;

  const generateQuestion = () => {
    const op = selectedMode === "MIX" ? (Math.random() < 0.5 ? "MUL" : "DIV") : selectedMode;
    let left, right, answer;
    if (op === "MUL") {
      left = randInt(2, 6);
      right = randInt(2, 6);
      answer = left * right;
    } else {
      right = randInt(2, 6);
      answer = randInt(2, 6);
      left = right * answer;
    }

    const choices = new Set([answer]);
    const offsets = [-3, -2, -1, 1, 2, 3, 4, -4, right, -right];
    while (choices.size < 4) {
      const candidate = answer + offsets[randInt(0, offsets.length - 1)];
      if (candidate > 0) choices.add(candidate);
    }

    const shuffled = Array.from(choices).sort(() => Math.random() - 0.5);
    return { op, left, right, answer, choices: shuffled };
  };

  const renderVisualHint = (q) => {
    elDotGroups.innerHTML = "";
    const isDiv = q.op === "DIV";
    const groupCount = isDiv ? q.right : Math.min(q.left, q.right);
    const perGroup = isDiv ? q.answer : Math.max(q.left, q.right);

    elHintText.textContent = isDiv
      ? `Bagi ${q.left} titik sama rata ke dalam ${q.right} kotak:`
      : `${groupCount} kelompok berisi ${perGroup} titik (${q.left} × ${q.right}):`;

    for (let g = 0; g < groupCount; g++) {
      const box = document.createElement("div");
      box.className = "dot-group";
      for (let d = 0; d < perGroup; d++) {
        const dot = document.createElement("span");
        dot.className = isDiv ? "math-dot div-dot" : "math-dot";
        box.appendChild(dot);
      }
      elDotGroups.appendChild(box);
    }
  };

  const loadQuestion = () => {
    currentQ = generateQuestion();
    answered = false;
    elHintPanel.hidden = true;
    elHintBtn.textContent = "💡 Bantuan Visual";
    elFeedback.hidden = true;
    elNextBtn.hidden = true;

    const isDiv = currentQ.op === "DIV";
    elBadge.textContent = isDiv ? "Pembagian (÷)" : "Perkalian (×)";
    elBadge.className = isDiv ? "chip chip-blue" : "chip chip-red";
    elEq.textContent = `${currentQ.left} ${isDiv ? "÷" : "×"} ${currentQ.right} = ?`;
    renderVisualHint(currentQ);

    elChoices.innerHTML = "";
    currentQ.choices.forEach((val) => {
      const btn = document.createElement("button");
      btn.type = "button";
      btn.className = "choice-btn";
      btn.textContent = String(val);
      btn.addEventListener("click", () => handleAnswer(val, btn));
      elChoices.appendChild(btn);
    });
  };

  const handleAnswer = (val, clickedBtn) => {
    if (answered) return;
    answered = true;
    const isCorrect = val === currentQ.answer;
    const sym = currentQ.op === "DIV" ? "÷" : "×";

    elChoices.querySelectorAll("button").forEach((b) => {
      b.disabled = true;
      const num = Number(b.textContent);
      if (num === currentQ.answer) {
        b.classList.add("correct");
        b.textContent = `✓ ${num}`;
      } else if (b === clickedBtn && !isCorrect) {
        b.classList.add("wrong");
      }
    });

    if (isCorrect) {
      combo += 1;
      score += 1;
      const praise =
        combo >= 5 ? "Keren sekali! Kombo super hebat!" :
        combo >= 3 ? "Mantap! Pertahankan terus!" :
        "Tepat sekali! Hebat!";
      elFeedback.textContent = `✓ ${praise}`;
      elFeedback.className = "demo-feedback ok";
    } else {
      combo = 0;
      elFeedback.textContent = `Belum tepat, yuk ingat: ${currentQ.left} ${sym} ${currentQ.right} = ${currentQ.answer}`;
      elFeedback.className = "demo-feedback err";
    }

    elCombo.textContent = `🔥 Kombo x${combo}`;
    elScore.textContent = `★ ${score} Benar`;
    elFeedback.hidden = false;
    elNextBtn.hidden = false;
  };

  document.querySelectorAll(".op-tab").forEach((tab) => {
    tab.addEventListener("click", () => {
      document.querySelectorAll(".op-tab").forEach((t) => t.classList.remove("active"));
      tab.classList.add("active");
      selectedMode = tab.dataset.op;
      loadQuestion();
    });
  });

  elHintBtn?.addEventListener("click", () => {
    elHintPanel.hidden = !elHintPanel.hidden;
    elHintBtn.textContent = elHintPanel.hidden ? "💡 Bantuan Visual" : "Sembunyikan Bantuan";
  });

  elNextBtn?.addEventListener("click", loadQuestion);

  loadQuestion();
})();
