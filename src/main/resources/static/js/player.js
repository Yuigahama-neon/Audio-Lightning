document.addEventListener("play", (event) => {
  const target = event.target;
  if (target.tagName !== "AUDIO") {
    return;
  }

  document.querySelectorAll("audio").forEach((audio) => {
    if (audio !== target) {
      audio.pause();
    }
  });
}, true);
