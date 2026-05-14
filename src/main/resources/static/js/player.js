(function () {
  const players = Array.from(document.querySelectorAll("[data-audio-player]"));

  const formatTime = (value) => {
    if (!Number.isFinite(value) || value < 0) {
      return "0:00";
    }

    const totalSeconds = Math.floor(value);
    const minutes = Math.floor(totalSeconds / 60);
    const seconds = String(totalSeconds % 60).padStart(2, "0");
    return `${minutes}:${seconds}`;
  };

  const pauseOtherAudio = (currentAudio) => {
    document.querySelectorAll("audio").forEach((audio) => {
      if (audio !== currentAudio) {
        audio.pause();
      }
    });
  };

  players.forEach((root) => {
    const audio = root.querySelector("audio.custom-player__audio");
    const ui = root.querySelector(".custom-player__ui");
    const playButton = root.querySelector("[data-audio-play]");
    const playIcon = root.querySelector("[data-audio-play-icon]");
    const seek = root.querySelector("[data-audio-seek]");
    const currentTime = root.querySelector("[data-audio-current]");
    const durationTime = root.querySelector("[data-audio-duration]");
    const muteButton = root.querySelector("[data-audio-mute]");
    const muteIcon = root.querySelector("[data-audio-mute-icon]");
    const volume = root.querySelector("[data-audio-volume]");

    if (!audio || !ui || !playButton || !seek || !currentTime || !durationTime || !muteButton || !volume) {
      return;
    }

    let lastVolume = audio.volume > 0 ? audio.volume : 1;

    const updatePlayState = () => {
      const isPlaying = !audio.paused && !audio.ended;
      root.classList.toggle("is-playing", isPlaying);
      playButton.setAttribute("aria-label", isPlaying ? "Пауза" : "Воспроизвести");

      playIcon?.setAttribute("data-state", isPlaying ? "pause" : "play");
    };

    const updateMuteState = () => {
      const isMuted = audio.muted || audio.volume === 0;
      const volumeProgress = isMuted ? 0 : audio.volume * 100;

      root.classList.toggle("is-muted", isMuted);
      muteButton.setAttribute("aria-label", isMuted ? "Включить звук" : "Отключить звук");
      muteIcon?.setAttribute("data-state", isMuted ? "muted" : "volume");
      volume.value = String(Math.round(volumeProgress));
      volume.style.setProperty("--volume-progress", `${volumeProgress}%`);
    };

    const updateProgress = () => {
      const duration = Number.isFinite(audio.duration) ? audio.duration : 0;
      const current = Number.isFinite(audio.currentTime) ? audio.currentTime : 0;
      const progress = duration > 0 ? (current / duration) * 100 : 0;

      seek.value = String(progress);
      seek.style.setProperty("--progress", `${progress}%`);
      currentTime.textContent = formatTime(current);
      durationTime.textContent = formatTime(duration);
      seek.disabled = duration <= 0;
    };

    root.classList.add("is-enhanced");
    ui.hidden = false;
    audio.controls = false;
    updateProgress();
    updatePlayState();
    updateMuteState();

    playButton.addEventListener("click", () => {
      if (audio.paused || audio.ended) {
        try {
          const playRequest = audio.play();

          if (playRequest && typeof playRequest.catch === "function") {
            playRequest.catch(() => {
              updatePlayState();
            });
          }
        } catch (error) {
          updatePlayState();
        }
      } else {
        audio.pause();
      }
    });

    muteButton.addEventListener("click", () => {
      root.classList.add("is-volume-open");

      if (audio.muted || audio.volume === 0) {
        audio.volume = lastVolume > 0 ? lastVolume : 0.7;
        audio.muted = false;
      } else {
        lastVolume = audio.volume;
        audio.muted = true;
      }

      updateMuteState();
    });

    volume.addEventListener("input", () => {
      root.classList.add("is-volume-open");

      const nextVolume = Math.max(0, Math.min(100, Number(volume.value))) / 100;

      audio.volume = nextVolume;
      audio.muted = nextVolume === 0;

      if (nextVolume > 0) {
        lastVolume = nextVolume;
      }

      updateMuteState();
    });

    seek.addEventListener("input", () => {
      const duration = Number.isFinite(audio.duration) ? audio.duration : 0;

      if (duration <= 0) {
        return;
      }

      audio.currentTime = (Number(seek.value) / 100) * duration;
      updateProgress();
    });

    audio.addEventListener("play", () => {
      pauseOtherAudio(audio);
      updatePlayState();
    });

    audio.addEventListener("pause", updatePlayState);
    audio.addEventListener("ended", updatePlayState);
    audio.addEventListener("timeupdate", updateProgress);
    audio.addEventListener("loadedmetadata", updateProgress);
    audio.addEventListener("durationchange", updateProgress);
    audio.addEventListener("volumechange", updateMuteState);
    audio.addEventListener("error", () => {
      root.classList.add("has-error");
      seek.disabled = true;
    });
  });
})();
