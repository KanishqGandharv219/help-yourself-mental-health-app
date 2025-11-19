from langchain_community.document_loaders import WebBaseLoader
from langchain_community.vectorstores import Chroma
from langchain_ollama import ChatOllama
from langchain_ollama import OllamaEmbeddings
from langchain_core.runnables import RunnablePassthrough
from langchain_core.output_parsers import StrOutputParser
from langchain_core.prompts import ChatPromptTemplate
from langchain.text_splitter import CharacterTextSplitter
from langchain.memory import ConversationBufferMemory
from langchain.schema import HumanMessage, AIMessage
import logging
import json
import re
from collections import deque

# Configure logging
logging.basicConfig(level=logging.INFO)
logger = logging.getLogger(__name__)

# Curated therapeutic resources
THERAPY_RESOURCES = [
    "https://www.apa.org/topics/therapy/psychotherapy-approaches",
    "https://www.nimh.nih.gov/health/topics/psychotherapies",
    "https://www.psychiatry.org/patients-families/psychotherapy"
]

# Exclusion list for organizational references
BLACKLISTED_ENTITIES = [
    "mpower", "thelivelovelaughfoundation",
    "depwd", "pib.gov", "mpowerminds"
]

class ContextManager:
    def __init__(self, window_size=6):
        self.window = deque(maxlen=window_size)
        self.theme_tracker = {}
        
    def update(self, exchange):
        """Add new exchange and track themes"""
        self.window.append(exchange)
        self._track_themes(exchange)
        
    def _track_themes(self, exchange):
        """Track conversation themes"""
        # Simple theme detection based on keywords
        themes = {
            'anxiety': r'\b(anxious|worried|panic|stress)\b',
            'depression': r'\b(sad|depressed|hopeless|down)\b',
            'relationships': r'\b(relationship|family|friend|partner)\b',
            'self_esteem': r'\b(worthless|confidence|self-esteem)\b',
            'trauma': r'\b(trauma|abuse|ptsd|flashback)\b'
        }
        
        text = exchange.lower()
        for theme, pattern in themes.items():
            if re.search(pattern, text, re.I):
                self.theme_tracker[theme] = self.theme_tracker.get(theme, 0) + 1
                
    def get_context(self):
        """Get current conversation context and themes"""
        return {
            "recent_exchanges": list(self.window),
            "dominant_themes": sorted(
                self.theme_tracker.items(),
                key=lambda x: x[1],
                reverse=True
            )[:3]
        }

class TherapistBot:
    def __init__(self):
        self.memory = ConversationBufferMemory(return_messages=True)
        self.model_local = ChatOllama(
            model="qwen2.5:latest",
            temperature=0.8
        )
        self.crisis_mode = False
        self.context_manager = ContextManager()
        self.setup_embeddings()
        
    # Enhanced crisis keywords with patterns and severity levels
    CRISIS_KEYWORDS = {
        "immediate_suicide": {
            "patterns": [
                r"\b(kill|end|hurt)\s+(myself|my\s+life)\b",
                r"\b(want\s+to\s+die|going\s+to\s+die)\b",
                r"\b(suicide|suicidal)\b",
                r"\b(better\s+off\s+dead|don't\s+want\s+to\s+live)\b",
                r"\b(plan\s+to\s+(die|kill))\b"
            ],
            "severity": "immediate"
        },
        "self_harm": {
            "patterns": [
                r"\b(cut|cutting|harm)\s+(myself|my)\b",
                r"\b(self\s*harm|self\s*injury)\b",
                r"\b(hurt\s+myself)\b"
            ],
            "severity": "high"
        },
        "severe_depression": {
            "patterns": [
                r"\b(hopeless|worthless|useless)\b",
                r"\b(no\s+point|nothing\s+matters)\b",
                r"\b(can't\s+go\s+on|give\s+up)\b",
                r"\b(lost\s+all\s+hope)\b"
            ],
            "severity": "high"
        },
        "panic_anxiety": {
            "patterns": [
                r"\b(panic\s+attack|anxiety\s+attack)\b",
                r"\b(can't\s+breathe|heart\s+racing)\b",
                r"\b(going\s+crazy|losing\s+my\s+mind)\b",
                r"\b(chest\s+pain|dizzy)\b"
            ],
            "severity": "medium"
        }
    }
    
    # Comprehensive helpline information
    HELPLINE_INFO = {
        "immediate_suicide": {
            "message": "I'm deeply concerned about what you've shared with me. Your life has value and meaning, and I want you to get immediate support. Please contact one of these crisis helplines right now - they're available 24/7 and staffed by trained professionals who care:",
            "contacts": [
                {"name": "KIRAN Mental Health Helpline", "number": "1800-599-0019", "available": "24/7"},
                {"name": "Tele-MANAS", "number": "14416", "available": "24/7"},
                {"name": "AASRA Suicide Prevention", "number": "022-27546669", "available": "24/7"},
                {"name": "Vandrevala Foundation", "number": "9999666555", "available": "24/7"},
                {"name": "1Life Crisis Helpline", "number": "78930-78930", "available": "24/7"}
            ],
            "follow_up": "If you're in immediate danger, please also consider going to your nearest emergency room or calling emergency services. You don't have to face this alone."
        },
        "self_harm": {
            "message": "I hear that you're in pain and considering hurting yourself. These feelings are valid, but there are safer ways to cope with them. Please reach out to these supportive services:",
            "contacts": [
                {"name": "KIRAN Helpline", "number": "1800-599-0019", "available": "24/7"},
                {"name": "iCALL Psychosocial Helpline", "number": "022-25521111", "available": "Mon-Sat 10am-8pm"},
                {"name": "Tele-MANAS", "number": "14416", "available": "24/7"}
            ],
            "follow_up": "Consider reaching out to a trusted friend, family member, or mental health professional as well."
        },
        "panic_anxiety": {
            "message": "It sounds like you're experiencing intense anxiety or panic. These feelings are temporary and will pass. Here are some immediate coping strategies and support numbers:",
            "contacts": [
                {"name": "Tele-MANAS", "number": "14416", "available": "24/7"},
                {"name": "KIRAN Helpline", "number": "1800-599-0019", "available": "24/7"}
            ],
            "follow_up": "Try the 4-7-8 breathing technique: Breathe in for 4 counts, hold for 7, exhale for 8. Repeat until you feel calmer."
        }
    }
    
    def detect_crisis(self, text):
        """Enhanced hierarchical crisis assessment"""
        text = text.lower().strip()
        
        # Immediate risk patterns
        suicide_regex = r"""
            (\b(end|kill|harm)\s+(my|this)\s*(life|self)\b)|
            (\b(no\s+reason\s+to\s+live)\b)|
            (\b(final|suicide)\s+(attempt|plan)\b)
        """
        
        # Emotional distress patterns
        depression_regex = r"""
            (\b(can't\s+go\s+on)\b)|
            (\b(lost\s+all\s+hope)\b)|
            (\b(worthless|hopeless)\b)
        """
        
        # Anxiety patterns
        anxiety_regex = r"""
            (\b(panic(\s+attack)?)\b)|
            (\b(heart\s+racing)\b)|
            (\b(can't\s+breathe)\b)
        """
        
        crisis_levels = {
            'immediate': suicide_regex,
            'high': depression_regex,
            'medium': anxiety_regex
        }
        
        for level, pattern in crisis_levels.items():
            if re.search(pattern, text, re.VERBOSE|re.IGNORECASE):
                return {'category': level, 'severity': level}
                
        return None

    def context_filter(self, query):
        """Determine if RAG context should be used"""
        informational_triggers = [
            r"what (is|are)",
            r"how to",
            r"explain",
            r"define",
            r"resources for"
        ]
        
        personal_sharing = [
            r"i (feel|think|need)",
            r"my (life|family|job)",
            r"struggling with",
            r"can't handle"
        ]
        
        if any(re.search(p, query, re.I) for p in informational_triggers):
            return True
        elif any(re.search(p, query, re.I) for p in personal_sharing):
            return False
        return None

    def sanitize_response(self, text):
        """Remove unwanted organizational references"""
        for entity in BLACKLISTED_ENTITIES:
            text = re.sub(rf"\b{entity}\b", "[mental health resource]", text, flags=re.I)
        return text
    
    def get_crisis_response(self, crisis_info):
        """Generate comprehensive crisis response with helplines."""
        category = crisis_info["category"]
        
        if category in self.HELPLINE_INFO:
            info = self.HELPLINE_INFO[category]
            response = info["message"] + "\n\n"
            response += "Crisis Support Numbers:\n"
            
            for contact in info["contacts"]:
                response += f"{contact['name']}: {contact['number']} ({contact['available']})\n"
            
            response += f"\n{info['follow_up']}\n\n"
            response += "Remember: These conversations are confidential. You deserve support and care."
            
            self.crisis_mode = True
            return response
        
        return self.get_general_crisis_response()
    
    def get_general_crisis_response(self):
        """Fallback crisis response for unclassified crisis situations."""
        response = "I'm concerned about you and want to make sure you get the support you need. Please consider reaching out to these crisis support services:\n\n"
        response += "Emergency Mental Health Support:\n"
        response += "KIRAN Helpline: 1800-599-0019 (24/7)\n"
        response += "Tele-MANAS: 14416 (24/7)\n\n"
        response += "You don't have to go through this alone. Professional help is available."
        return response
    
    def setup_embeddings(self):
        """Initialize embeddings with curated therapeutic resources."""
        try:
            logger.info("Loading documents from therapeutic resources...")
            docs = []
            for url in THERAPY_RESOURCES:
                try:
                    loader = WebBaseLoader(url)
                    docs.extend(loader.load())
                except Exception as e:
                    logger.error(f"Error loading from {url}: {str(e)}")

            # Split documents into manageable chunks
            text_splitter = CharacterTextSplitter.from_tiktoken_encoder(
                chunk_size=500,  # Smaller chunks for more focused context
                chunk_overlap=50
            )
            doc_splits = text_splitter.split_documents(docs)

            logger.info("Creating embeddings and vector store...")
            embedding_model = OllamaEmbeddings(
                model='nomic-embed-text',
                base_url="http://localhost:11434"
            )
            
            # Create vector store
            self.vectorstore = Chroma.from_documents(
                documents=doc_splits,
                embedding=embedding_model
            )
            
            # Create retriever with similarity threshold
            self.retriever = self.vectorstore.as_retriever(
                search_kwargs={
                    "k": 3,  # Return top 3 most relevant chunks
                    "score_threshold": 0.7  # Only return relevant matches
                }
            )
            
            logger.info("Embeddings setup completed successfully")
        except Exception as e:
            logger.error(f"Error setting up embeddings: {str(e)}")
            raise

    def get_relevant_context(self, query):
        """Get relevant context from vector store with improved filtering."""
        try:
            # Get relevant documents
            docs = self.retriever.get_relevant_documents(query)
            
            # Extract and format relevant information
            context = "\n".join([
                f"- {doc.page_content.strip()}"
                for doc in docs
            ])
            
            return context if context.strip() else ""
        except Exception as e:
            logger.error(f"Error retrieving context: {str(e)}")
            return ""
    
    def generate_therapeutic_response(self, user_input):
        """Generate appropriate therapeutic response based on context."""
        # First check for crisis
        crisis_info = self.detect_crisis(user_input)
        if crisis_info:
            return self.get_crisis_response(crisis_info)
        
        # Update context manager
        self.context_manager.update(user_input)
        context_info = self.context_manager.get_context()
        
        # Determine if we should use RAG context
        use_context = self.context_filter(user_input)
        
        # Get conversation history
        history = self.memory.chat_memory.messages[-6:] if self.memory.chat_memory.messages else []
        conversation_context = ""
        if history:
            conversation_context = "\n".join([
                f"{'User' if isinstance(msg, HumanMessage) else 'Assistant'}: {msg.content}"
                for msg in history
            ])
        
        # Get relevant therapeutic context if needed
        context = self.get_relevant_context(user_input) if use_context else ""
        
        # Enhanced therapeutic prompt
        therapeutic_prompt = """You are an AI mental health assistant trained on various mental health guidelines and best practices. You provide empathetic support while maintaining clear boundaries about your role.

CORE THERAPEUTIC MODALITIES:
1. Person-Centered Therapy: Unconditional positive regard
2. CBT Elements: Cognitive restructuring guidance
3. DBT Skills: Distress tolerance techniques
4. Solution-Focused Brief Therapy: Goal-oriented questioning

RESPONSE GUIDELINES:
- Maintain 70/30 listening/guidance ratio
- Use reflective statements every 2-3 exchanges
- Employ open-ended questions for deeper exploration
- Normalize emotions without minimization
- Introduce coping strategies gradually
- Be clear about being an AI assistant

SAFETY PROTOCOLS:
- Immediate crisis detection override
- No organizational references unless explicitly requested
- Clear AI identity disclosure
- Professional boundary maintenance

CONVERSATION CONTEXT:
Recent exchanges: {conversation_context}

IDENTIFIED THEMES:
{', '.join(f"{theme}: {count}" for theme, count in context_info['dominant_themes'])}

{f"RELEVANT INFORMATION:\n{context}" if context else ""}

USER MESSAGE: {user_input}

Generate a response that:
1. Acknowledges and validates the user's feelings
2. Maintains appropriate therapeutic boundaries
3. Provides evidence-based support when appropriate
4. Is transparent about being an AI assistant"""

        try:
            response = self.model_local.invoke(therapeutic_prompt)
            response_text = response.content if hasattr(response, 'content') else str(response)
            
            # Sanitize response
            response_text = self.sanitize_response(response_text)
            
            # Add to memory
            self.memory.chat_memory.add_user_message(user_input)
            self.memory.chat_memory.add_ai_message(response_text)
            
            return response_text
            
        except Exception as e:
            logger.error(f"Error generating response: {e}")
            return "I'm having some technical difficulties right now. As an AI assistant, I want to ensure you receive reliable support. If you're experiencing distress, please reach out to a mental health professional or crisis helpline in your area."
    
    def chat(self, user_input):
        """Main chat interface."""
        if not user_input.strip():
            return "Hello! I am an AI mental health assistant trained on various mental health guidelines and best practices. I'm here to listen and provide support. How are you feeling today?"
        
        try:
            response = self.generate_therapeutic_response(user_input)
            return response
        except Exception as e:
            logger.error(f"Chat error: {e}")
            return "I apologize, but I'm experiencing some technical issues. If you're in crisis, please reach out to a mental health helpline or emergency services in your area."

    def clear_memory(self):
        """Clear the conversation memory and reset to initial state."""
        self.memory = ConversationBufferMemory(return_messages=True)
        self.crisis_mode = False
        logger.info("Conversation memory cleared")

# Initialize the bot
bot = TherapistBot()

# Example usage
if __name__ == "__main__":
    print("Mental Health Support Bot Ready")
    print("Type 'quit' to exit")
    print("-" * 50)
    
    while True:
        user_input = input("\nYou: ")
        if user_input.lower() in ['quit', 'exit', 'bye']:
            print("Bot: Take care of yourself. Remember, support is always available when you need it.")
            break
        
        response = bot.chat(user_input)
        print(f"\nBot: {response}") 