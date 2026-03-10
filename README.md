# VoiceBridge

**VoiceBridge** is an accessibility-focused Android app designed to help blind or visually impaired users quickly contact a trusted person. With a simple voice command or a physical trigger, the app opens WhatsApp and starts the process of initiating a video call to a predefined contact.

The goal of VoiceBridge is to reduce the number of steps required to place a call and provide clear audio feedback so users who cannot easily navigate a smartphone interface can still communicate quickly and confidently.

---

## ✨ Key Features

* 🎙 **Voice Command Trigger**
  Say a simple phrase like **"Call Arpan"** to initiate a call.

* 🔘 **Physical Button Trigger**
  Hold the **Volume Up button for 3 seconds** to start the call process.

* 📞 **WhatsApp Integration**
  Automatically opens a WhatsApp chat with a predefined contact.

* 🔊 **Text-to-Speech Feedback**
  The app confirms actions with spoken messages.

* 📳 **Vibration Feedback**
  Optional vibration confirms that commands were detected.

* ♿ **Accessibility First Design**
  Built specifically with blind and visually impaired users in mind.

---

## 🧠 How It Works

VoiceBridge provides two simple ways to start a call:

### Voice Trigger

User says:

`Call Arpan`

Flow:

Voice command detected
→ VoiceBridge confirms with speech
→ WhatsApp chat opens with the trusted contact

---

### Physical Trigger

User holds the **Volume Up button for 3 seconds**

Flow:

Volume hold detected
→ VoiceBridge confirms action
→ WhatsApp chat opens with the trusted contact

---

## 📦 Project Structure

```
com.voicebridge
│
├── MainActivity
│
├── service
│   ├── VoiceCommandService
│   └── WhatsAppAccessibilityService
│
├── voice
│   └── SpeechRecognizerManager
│
├── call
│   └── WhatsAppCaller
│
└── utils
    ├── TTSManager
    └── VibrationHelper
```

---

## ⚙️ Tech Stack

* **Language:** Kotlin
* **Platform:** Android
* **Minimum SDK:** 26
* **Architecture:** Modular package structure
* **Accessibility APIs:** Android Accessibility Service
* **Speech:** Android SpeechRecognizer (planned)

---

## 🚀 Current Status

VoiceBridge is currently in **early development**.
The repository focuses on building a clean foundation before implementing the full functionality.

Implemented so far:

* Project architecture
* Accessibility service scaffold
* WhatsApp launch utility
* Text-to-speech helper
* Core service structure

---

## 🛠 Planned Features

* Wake-word voice recognition
* Volume button long-press detection
* Automatic video call initiation
* Offline speech recognition
* Improved accessibility onboarding
* Better feedback through speech and vibration
* Support for multiple trusted contacts

---

## 🎯 Vision

VoiceBridge aims to provide a **simple, reliable, and accessible way for visually impaired users to reach someone they trust**. By reducing complex phone navigation into a single voice command or button hold, the app helps users stay connected and feel safer in everyday situations.

---

## 🤝 Contributing

Contributions, feedback, and suggestions are welcome.
If you'd like to help improve accessibility tools like VoiceBridge, feel free to open an issue or submit a pull request.

---

## 📄 License

This project is open source and available under the **MIT License**.
