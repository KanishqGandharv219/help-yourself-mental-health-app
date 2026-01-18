# Help Yourself - Mental Health AI Assistant

A comprehensive Android application designed to provide mental health support, wellness tracking, and AI-powered assistance. Built with modern Android development practices and a Python backend server with RAG (Retrieval Augmented Generation) capabilities.

## 📱 Overview

**Help Yourself** (also known as Custom AI Assistant) is an intelligent mental health companion app that combines:
- **AI-Powered Conversations**: Chat with an empathetic AI assistant powered by local Ollama models with RAG
- **Mental Health Assessments**: Self-assessment tests for depression, anxiety, and stress
- **Wellness Tracking**: Track and monitor your mental health metrics over time
- **Resource Discovery**: Find nearby therapy centers and mental health resources using Google Maps
- **Educational Resources**: Access curated mental health and academic resources
- **Crisis Support**: Built-in crisis detection and helpline information for India

## ✨ Features

### Core Features
- 🤖 **AI Chat Assistant**: Conversational AI with context-aware responses using RAG
<img width="108" height="240" alt="Screenshot_20250703_022714" src="https://github.com/user-attachments/assets/1bc1a23e-de83-4b20-bc32-a8eeaec285ae" />
<img width="1080" height="2400" alt="Screenshot_20250703_022323" src="https://github.com/user-attachments/assets/31c74e6a-eb1c-4061-9959-72d825904379" />
<img width="1080" height="2400" alt="Screenshot_20250703_022323" src="https://github.com/user-attachments/assets/6704a397-afef-4b4c-be3d-b2a89606f1a0" />
<img width="1080" height="2400" alt="Screenshot_20250703_022558" src="https://github.com/user-attachments/assets/9f32f6e0-07a3-4bc9-9bd6-748f76803ca8" />
<img width="1080" height="2400" alt="Screenshot_20250703_022639" src="https://github.com/user-attachments/assets/127cc415-2e62-4cd3-ab48-3fec178ca0bc" />

- 📊 **Mental Health Assessments**: <img width="1080" height="2400" alt="Screenshot_20250703_022911" src="https://github.com/user-attachments/assets/5ced87c2-f732-4459-9aa8-d8f34e169258" />
<img width="1080" height="2400" alt="Screenshot_20250703_022937" src="https://github.com/user-attachments/assets/26caca83-4d3d-43a5-941f-0d506d57f567" />

  - Depression Test (PHQ-9 based)
  <img width="1080" height="2400" alt="Screenshot_20250703_023158" src="https://github.com/user-attachments/assets/38babcff-f79f-4a64-a011-0c42e53d0e87" />

  - Anxiety Test (GAD-7 based)
  <img width="1080" height="2400" alt="Screenshot_20250703_023219" src="https://github.com/user-attachments/assets/a1a28dac-6ff9-446d-9612-c6aefc6ece76" />

  - Stress Test
  <img width="1080" height="2400" alt="Screenshot_20250703_023233" src="https://github.com/user-attachments/assets/d48559f5-2f21-4624-bdb4-7151d1635e84" />

- 📈 **Wellness Tracking**: Track depression, anxiety, and stress levels over time
<img width="1080" height="2400" alt="Screenshot_20250703_023507" src="https://github.com/user-attachments/assets/ca68d92e-c559-401c-8889-1a35a3acce65" />

- 🗺️ **Nearby Resources**: Find therapy centers and mental health facilities using Google Maps
<img width="1080" height="2400" alt="Screenshot_20250703_023118" src="https://github.com/user-attachments/assets/c70e8709-5772-4043-a346-9efb9e6598fc" />

- 📚 **Resource Library**: Access mental health and academic resources
<img width="1080" height="2400" alt="Screenshot_20250703_022747" src="https://github.com/user-attachments/assets/89429edf-7ae1-4a34-a7ad-14fd57908d43" />

- 🧘 **Breathing Exercises**: Guided breathing exercises for stress relief
![Screenshot_2026-01-18-10-43-57-77_957b39ce7caa0d8ecb2d9a9eb9b96f66](https://github.com/user-attachments/assets/09cf43a2-7330-4cc5-affd-9dd4753611d3)

- 📝 **Personalized Reviews**: AI-generated personalized mental health reviews using Google Gemini
<img width="1080" height="2400" alt="Screenshot_20250703_023547" src="https://github.com/user-attachments/assets/3f9ef0c9-8fb3-44c7-bd34-5d64c6fba105" />

- 🔍 **Web Search Integration**: Tavily API integration for real-time information retrieval
<img width="391" height="129" alt="image" src="https://github.com/user-attachments/assets/347529ea-528c-4357-9d9d-6eb5468183f4" />

- 🔐 **Firebase Authentication**: Secure user authentication with Google, Email, and Phone sign-in
<img width="640" height="640" alt="image" src="https://github.com/user-attachments/assets/2ac8a937-780a-4994-ac9b-83d2c96d33b8" />
<img width="1080" height="2400" alt="Screenshot_20250703_022836" src="https://github.com/user-attachments/assets/d1cac759-21c2-4dac-b1e5-ecd7ab189ce4" />

### Technical Features
- **Modern Android Architecture**: MVVM pattern with Jetpack Compose
- **Dependency Injection**: Dagger Hilt for clean architecture
- **Local AI Processing**: Ollama integration for privacy-focused AI responses
- **RAG Implementation**: Retrieval Augmented Generation for context-aware responses
- **Real-time Data**: Firebase Realtime Database for data synchronization
- **Offline Support**: Local caching and offline capabilities

## 🏗️ Architecture

### Android App
- **Language**: Kotlin
- **UI Framework**: Jetpack Compose
- **Architecture**: MVVM (Model-View-ViewModel)
- **Dependency Injection**: Dagger Hilt
- **Networking**: Retrofit + OkHttp
- **Database**: Room Database (local) + Firebase Realtime Database (cloud)
- **Authentication**: Firebase Auth

### Backend Server
- **Framework**: Flask (Python)
- **AI Model**: Ollama (Qwen2.5, Nomic Embed)
- **Vector Database**: ChromaDB
- **RAG**: LangChain with Ollama embeddings
- **CORS**: Enabled for cross-origin requests

## 📋 Prerequisites

### For Android Development
- Android Studio Hedgehog (2023.1.1) or later
- JDK 17 or later
- Android SDK (API 24+)
- Gradle 8.0+

### For Backend Server
- Python 3.10 or later
- Ollama installed and running locally
- Required Python packages (see `server/requirements.txt`)

### API Keys Required
- **Firebase**: For authentication and database
- **Google Maps API**: For location services
- **Google Gemini API**: For personalized reviews
- **Tavily API**: For web search integration

## 🚀 Getting Started

### 1. Clone the Repository

```bash
git clone <repository-url>
cd final
```

### 2. Android App Setup

#### Step 1: Configure Firebase
1. Create a Firebase project at [Firebase Console](https://console.firebase.google.com/)
2. Add an Android app to your Firebase project
3. Download `google-services.json`
4. Place it in:
   - `app/compose-chatgpt-kotlin-android-chatbot/app/google-services.json`
   - `app/compose-chatgpt-kotlin-android-chatbot/app/src/main/google-services.json`

#### Step 2: Configure API Keys
1. Create `local.properties` file in `app/compose-chatgpt-kotlin-android-chatbot/`:
   ```properties
   sdk.dir=/path/to/your/android/sdk
   GEMINI_API_KEY=your_gemini_api_key_here
   TAVILY_API_KEY=your_tavily_api_key_here
   ```

2. Create `secrets.xml` in `app/compose-chatgpt-kotlin-android-chatbot/app/src/main/res/values/`:
   ```xml
   <?xml version="1.0" encoding="utf-8"?>
   <resources>
       <string name="google_maps_api_key">your_google_maps_api_key_here</string>
   </resources>
   ```

#### Step 3: Build and Run
```bash
cd app/compose-chatgpt-kotlin-android-chatbot
./gradlew assembleDebug
```

Or open the project in Android Studio and run it.

### 3. Backend Server Setup

#### Step 1: Install Ollama
```bash
# Visit https://ollama.ai/ for installation instructions
# Pull required models
ollama pull qwen2.5:latest
ollama pull nomic-embed-text
```

#### Step 2: Install Python Dependencies
```bash
cd server
pip install -r requirements.txt
```

#### Step 3: Configure Server
Update the server URL in `NetworkModule.kt` if needed:
- For emulator: `http://10.0.2.2:5002/`
- For physical device: Update `LOCAL_DEVICE_URL` with your computer's IP

#### Step 4: Run the Server
```bash
cd server
python server.py
# Server runs on http://localhost:5002
```

## 📁 Project Structure

```
final/
├── app/
│   └── compose-chatgpt-kotlin-android-chatbot/
│       ├── app/
│       │   ├── src/main/
│       │   │   ├── java/com/helpyourself/com/
│       │   │   │   ├── data/          # Data layer (repositories, API services)
│       │   │   │   ├── di/            # Dependency injection modules
│       │   │   │   ├── ui/            # UI components (Compose screens)
│       │   │   │   │   ├── conversations/  # Chat screens
│       │   │   │   │   ├── screens/      # Feature screens
│       │   │   │   │   └── resources/    # Resource screens
│       │   │   │   └── utils/         # Utility classes
│       │   │   └── res/               # Resources (layouts, drawables, etc.)
│       │   └── build.gradle
│       ├── build.gradle
│       ├── local.properties           # API keys (excluded from Git)
│       └── README.md
├── server/
│   ├── server.py                      # Main Flask server
│   ├── app.py                         # Alternative server implementation
│   ├── direct_ollama.py              # Direct Ollama integration
│   ├── requirements.txt              # Python dependencies
│   └── chroma_db/                     # Vector database storage
├── ollama_rag/
│   ├── app.py                         # RAG Flask app
│   ├── modelrag.py                    # RAG model implementation
│   └── simple_rag.py                 # Simple RAG implementation
├── fine_tuned/                        # Fine-tuned model files
├── .gitignore                         # Git ignore rules
└── README.md                          # This file
```

## 🔧 Configuration

### Environment Variables

The app uses `local.properties` for sensitive configuration. This file is excluded from version control. Create it with:

```properties
# Android SDK location
sdk.dir=/path/to/android/sdk

# API Keys
GEMINI_API_KEY=your_gemini_api_key
TAVILY_API_KEY=your_tavily_api_key
```

### Firebase Configuration

1. Enable Authentication providers in Firebase Console:
   - Google Sign-In
   - Email/Password
   - Phone Authentication

2. Set up Firebase Realtime Database rules (see `database.rules.json`)

3. Configure OAuth clients for Google Sign-In

## 🛠️ Technologies Used

### Android
- **Jetpack Compose**: Modern declarative UI framework
- **Kotlin Coroutines**: Asynchronous programming
- **Dagger Hilt**: Dependency injection
- **Retrofit**: HTTP client
- **Room**: Local database
- **Firebase**: Authentication, Realtime Database
- **Google Maps SDK**: Location services
- **Material Design 3**: UI components

### Backend
- **Flask**: Python web framework
- **Ollama**: Local LLM inference
- **LangChain**: RAG framework
- **ChromaDB**: Vector database
- **Tiktoken**: Tokenization

### APIs
- **Google Gemini API**: AI-powered reviews
- **Tavily API**: Web search
- **Google Maps API**: Location services

## 🔒 Security

- All API keys are stored in `local.properties` (excluded from Git)
- Firebase security rules implemented
- No sensitive data in source code
- See `SECURITY.md` for more details

## 📝 Development Notes

### Running the Backend
The backend server must be running for the app to function properly. Default port is `5002`.

### Testing
- Unit tests: `./gradlew test`
- Instrumented tests: `./gradlew connectedAndroidTest`

### Building Release APK
```bash
./gradlew assembleRelease
```

## 🤝 Contributing

Contributions are welcome! Please feel free to submit a Pull Request. For major changes, please open an issue first to discuss what you would like to change.

1. Fork the repository
2. Create your feature branch (`git checkout -b feature/AmazingFeature`)
3. Commit your changes (`git commit -m 'Add some AmazingFeature'`)
4. Push to the branch (`git push origin feature/AmazingFeature`)
5. Open a Pull Request

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](app/compose-chatgpt-kotlin-android-chatbot/LICENSE) file for details.

## 👤 Author

**Kanishq Gandharv**

- Email: kanishqgandharv@gmail.com

## 🙏 Acknowledgments

- [Jetpack Compose](https://developer.android.com/jetpack/compose)
- [Firebase](https://firebase.google.com/)
- [Ollama](https://ollama.ai/)
- [LangChain](https://www.langchain.com/)
- [Material Design 3](https://m3.material.io/)

## ⚠️ Important Notes

### Crisis Support
This app includes mental health resources and crisis helplines for India:
- **Tele-Manas**: 14416 (24×7 Government helpline)
- **Kiran**: 1800-599-0019 (National Mental Health Rehab Helpline)
- **Aasra**: +91-22-27546669 (24×7 Suicide Prevention)

**This app is not a replacement for professional mental health care. If you're experiencing a mental health crisis, please contact a licensed mental health professional or emergency services immediately.**

### API Keys
- Never commit API keys to version control
- All sensitive keys are stored in `local.properties` which is excluded via `.gitignore`
- Create your own API keys from respective providers

## 📞 Support

For issues, questions, or contributions:
- Open an issue on GitHub
- Contact: kanishqgandharv@gmail.com

---

**Made with ❤️ for mental health awareness and support**

