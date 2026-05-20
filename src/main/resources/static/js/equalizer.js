(function () {
  const STORAGE_KEY = "audio-lightning:eq-settings";
  const AudioContextClass = window.AudioContext || window.webkitAudioContext;
  const isSupported = Boolean(AudioContextClass);
  const graphRegistry = new WeakMap();
  let sharedContext = null;

  const bands = [
    { id: "60", frequency: 60, type: "lowshelf", q: 0.8 },
    { id: "230", frequency: 230, type: "peaking", q: 1 },
    { id: "910", frequency: 910, type: "peaking", q: 1 },
    { id: "4000", frequency: 4000, type: "peaking", q: 1 },
    { id: "14000", frequency: 14000, type: "highshelf", q: 0.8 }
  ];

  const presets = {
    flat: { "60": 0, "230": 0, "910": 0, "4000": 0, "14000": 0 },
    bassBoost: { "60": 6, "230": 3, "910": 0, "4000": 0, "14000": 1 },
    vocal: { "60": -2, "230": -1, "910": 4, "4000": 3, "14000": 1 },
    rock: { "60": 4, "230": 2, "910": -1, "4000": 3, "14000": 4 },
    electronic: { "60": 5, "230": 1, "910": -2, "4000": 2, "14000": 5 },
    soft: { "60": -2, "230": 0, "910": 1, "4000": -1, "14000": -2 }
  };

  const defaultSettings = {
    enabled: true,
    preset: "flat",
    bands: { ...presets.flat }
  };

  const players = Array.from(document.querySelectorAll("[data-audio-player]"));

  const clampGain = (value) => Math.max(-12, Math.min(12, Number(value) || 0));

  const normalizeSettings = (value) => {
    const nextBands = { ...defaultSettings.bands };

    bands.forEach((band) => {
      if (value?.bands && Object.prototype.hasOwnProperty.call(value.bands, band.id)) {
        nextBands[band.id] = clampGain(value.bands[band.id]);
      }
    });

    return {
      enabled: typeof value?.enabled === "boolean" ? value.enabled : defaultSettings.enabled,
      preset: value?.preset && (presets[value.preset] || value.preset === "custom") ? value.preset : defaultSettings.preset,
      bands: nextBands
    };
  };

  const loadSettings = () => {
    try {
      const raw = window.localStorage?.getItem(STORAGE_KEY);
      return raw ? normalizeSettings(JSON.parse(raw)) : normalizeSettings(defaultSettings);
    } catch (error) {
      return normalizeSettings(defaultSettings);
    }
  };

  let settings = loadSettings();

  const saveSettings = () => {
    try {
      window.localStorage?.setItem(STORAGE_KEY, JSON.stringify(settings));
    } catch (error) {
      // localStorage can be blocked; the equalizer still works for the current page.
    }
  };

  const formatDb = (value) => {
    const number = clampGain(value);
    return `${number > 0 ? "+" : ""}${number} dB`;
  };

  const getRefs = (root) => ({
    root,
    audio: root.querySelector("audio.custom-player__audio"),
    toggle: root.querySelector("[data-eq-toggle]"),
    panel: root.querySelector("[data-eq-panel]"),
    enabled: root.querySelector("[data-eq-enabled]"),
    preset: root.querySelector("[data-eq-preset]"),
    reset: root.querySelector("[data-eq-reset]"),
    sliders: Array.from(root.querySelectorAll("[data-eq-band]")),
    outputs: Array.from(root.querySelectorAll("[data-eq-output]")),
    message: root.querySelector("[data-eq-message]")
  });

  const setMessage = (refs, message) => {
    if (!refs.message) {
      return;
    }

    refs.message.textContent = message || "";
    refs.message.hidden = !message;
  };

  const setUnsupported = (refs, message) => {
    refs.root.classList.add("is-eq-unsupported");
    refs.toggle?.setAttribute("aria-disabled", "true");
    refs.enabled && (refs.enabled.disabled = true);
    refs.preset && (refs.preset.disabled = true);
    refs.reset && (refs.reset.disabled = true);
    refs.sliders.forEach((slider) => {
      slider.disabled = true;
    });
    setMessage(refs, message || "Эквалайзер недоступен в этом браузере");
  };

  const getContext = () => {
    if (!sharedContext) {
      sharedContext = new AudioContextClass();
    }

    return sharedContext;
  };

  const resumeContext = async (audioContext) => {
    if (audioContext.state === "suspended") {
      await audioContext.resume();
    }
  };

  const applySettingsToGraph = (graph) => {
    if (!graph?.filters || !graph.audioContext) {
      return;
    }

    const time = graph.audioContext.currentTime;

    bands.forEach((band) => {
      const filter = graph.filters[band.id];
      const gain = settings.enabled ? clampGain(settings.bands[band.id]) : 0;

      if (filter?.gain?.setTargetAtTime) {
        filter.gain.setTargetAtTime(gain, time, 0.015);
      } else if (filter?.gain) {
        filter.gain.value = gain;
      }
    });

    graph.enabled = settings.enabled;
    graph.settings = settings;
  };

  const applySettingsToGraphs = () => {
    players.forEach((root) => {
      const audio = root.querySelector("audio.custom-player__audio");
      const graph = audio ? graphRegistry.get(audio) : null;
      applySettingsToGraph(graph);
    });
  };

  const createGraph = async (refs) => {
    if (!isSupported) {
      setUnsupported(refs);
      return null;
    }

    if (!refs.audio) {
      return null;
    }

    const existing = graphRegistry.get(refs.audio);
    if (existing) {
      try {
        await resumeContext(existing.audioContext);
        applySettingsToGraph(existing);
        return existing;
      } catch (error) {
        refs.root.classList.add("is-eq-error");
        setMessage(refs, "Эквалайзер временно недоступен");
        return null;
      }
    }

    try {
      const audioContext = getContext();
      await resumeContext(audioContext);

      const source = audioContext.createMediaElementSource(refs.audio);
      const filters = {};
      let previousNode = source;

      bands.forEach((band) => {
        const filter = audioContext.createBiquadFilter();
        filter.type = band.type;
        filter.frequency.value = band.frequency;
        filter.Q.value = band.q;
        filter.gain.value = settings.enabled ? settings.bands[band.id] : 0;

        previousNode.connect(filter);
        previousNode = filter;
        filters[band.id] = filter;
      });

      const outputGain = audioContext.createGain();
      outputGain.gain.value = 1;
      previousNode.connect(outputGain);
      outputGain.connect(audioContext.destination);

      const graph = {
        audioContext,
        source,
        filters,
        outputGain,
        enabled: settings.enabled,
        settings
      };

      graphRegistry.set(refs.audio, graph);
      refs.root.classList.remove("is-eq-error");
      setMessage(refs, "");
      applySettingsToGraph(graph);
      return graph;
    } catch (error) {
      refs.root.classList.add("is-eq-error");
      setMessage(refs, "Эквалайзер не удалось включить для этого трека");
      return null;
    }
  };

  const syncPanel = (refs) => {
    refs.root.classList.toggle("is-eq-enabled", settings.enabled);

    if (refs.enabled) {
      refs.enabled.checked = settings.enabled;
    }

    if (refs.preset) {
      refs.preset.value = settings.preset;
    }

    refs.sliders.forEach((slider) => {
      const value = clampGain(settings.bands[slider.dataset.eqBand]);
      const progress = ((value + 12) / 24) * 100;

      slider.value = String(value);
      slider.style.setProperty("--eq-progress", `${progress}%`);
      slider.disabled = !settings.enabled || refs.root.classList.contains("is-eq-unsupported");
    });

    refs.outputs.forEach((output) => {
      output.textContent = formatDb(settings.bands[output.dataset.eqOutput]);
    });
  };

  const syncAllPanels = () => {
    players.forEach((root) => {
      const refs = getRefs(root);

      if (!refs.toggle || !refs.panel) {
        return;
      }

      syncPanel(refs);
    });
  };

  const setSettings = (nextSettings) => {
    settings = normalizeSettings(nextSettings);
    saveSettings();
    syncAllPanels();
    applySettingsToGraphs();
  };

  const openPanel = async (refs) => {
    refs.panel.hidden = false;
    refs.toggle?.setAttribute("aria-expanded", "true");
    refs.root.classList.add("is-eq-open");

    if (!isSupported) {
      setUnsupported(refs);
      return;
    }

    await createGraph(refs);
  };

  const closePanel = (refs) => {
    refs.panel.hidden = true;
    refs.toggle?.setAttribute("aria-expanded", "false");
    refs.root.classList.remove("is-eq-open");
  };

  players.forEach((root) => {
    const refs = getRefs(root);

    if (!refs.toggle || !refs.panel || !refs.audio) {
      return;
    }

    if (!isSupported) {
      setUnsupported(refs);
    }

    syncPanel(refs);

    refs.toggle.addEventListener("click", async () => {
      if (refs.panel.hidden) {
        await openPanel(refs);
      } else {
        closePanel(refs);
      }
    });

    refs.audio.addEventListener("play", () => {
      createGraph(refs);
    });

    refs.enabled?.addEventListener("change", () => {
      setSettings({ ...settings, enabled: refs.enabled.checked });
      createGraph(refs);
    });

    refs.preset?.addEventListener("change", () => {
      const preset = refs.preset.value;

      if (preset === "custom") {
        setSettings({ ...settings, preset: "custom" });
        return;
      }

      setSettings({
        enabled: settings.enabled,
        preset,
        bands: presets[preset] || presets.flat
      });
      createGraph(refs);
    });

    refs.reset?.addEventListener("click", () => {
      setSettings({
        enabled: true,
        preset: "flat",
        bands: presets.flat
      });
      createGraph(refs);
    });

    refs.sliders.forEach((slider) => {
      slider.addEventListener("input", () => {
        setSettings({
          enabled: settings.enabled,
          preset: "custom",
          bands: {
            ...settings.bands,
            [slider.dataset.eqBand]: clampGain(slider.value)
          }
        });
        createGraph(refs);
      });
    });
  });

  window.AudioLightningEqualizer = {
    applySettings: setSettings,
    getSettings: () => normalizeSettings(settings),
    reset: () => setSettings(defaultSettings),
    isSupported: () => isSupported
  };
})();
