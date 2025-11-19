# mental_health_chatbot.py

from langchain_community.document_loaders import WebBaseLoader
from langchain_community.vectorstores import Chroma
from langchain_ollama import ChatOllama
from langchain_ollama import OllamaEmbeddings
from langchain_core.runnables import RunnablePassthrough
from langchain_core.output_parsers import StrOutputParser
from langchain_core.prompts import ChatPromptTemplate
from langchain.text_splitter import CharacterTextSplitter
from langchain.memory import ConversationBufferMemory
import logging
import asyncio
import time
import os
import torch

# Configure GPU/CUDA settings
USE_GPU = torch.cuda.is_available()
GPU_DEVICE = 0 if USE_GPU else -1
if USE_GPU:
    os.environ["CUDA_VISIBLE_DEVICES"] = str(GPU_DEVICE)
    logging.info(f"GPU is available: {torch.cuda.get_device_name(0)}")
else:
    logging.info("GPU not available, using CPU")

# Configure logging
logging.basicConfig(level=logging.INFO)
logger = logging.getLogger(__name__)

async def process_question(question, retriever, after_rag_chain):
    print("\n" + "="*80)
    print(f"Processing question: {question}")
    print("="*80)
    
    # Step 1: Get relevant documents
    print("\nStep 1: Retrieving relevant information...")
    context = await retriever.aget_relevant_documents(question)
    print("Context retrieved successfully.")
    time.sleep(1)  # Small delay to ensure completion
    
    # Step 2: Generate response
    print("\nStep 2: Generating response...")
    answer = await after_rag_chain.ainvoke(question)
    print("Response generated successfully.")
    time.sleep(1)  # Small delay to ensure completion
    
    # Display results
    print("\nFINAL RESULTS:")
    print("="*80)
    print(f"Question: {question}")
    print("-"*40)
    print(f"Answer: {answer}")
    print("="*80)
    return answer

try:
    # Initialize the local LLM model with GPU support if available
    model_local = ChatOllama(
        model="qwen2.5:latest",
        base_url="http://localhost:11434",
        temperature=0.7,
        # Let Ollama decide whether to use GPU - it will use it if available
    )
    logger.info(f"Initialized Ollama model. GPU available: {USE_GPU}")

    # Define the system prompt to guide the chatbot's behavior
    system_prompt = ChatPromptTemplate.from_template(
        "You are a compassionate and supportive mental‐health therapist. "
        "Always respond with empathy, active listening, and non-judgmental language. "
        "Begin each conversation by asking how the user is feeling. "
        "If the user expresses any crisis or self-harm ideas, immediately suggest a suicide/crisis helpline. "
        "If you do not find an answer in your resources, say 'I am sorry, I do not have that information.'"
    )

    # URLs containing mental health resources
    urls = [
        "https://www.thelivelovelaughfoundation.org/find-help/helplines",
        "https://depwd.gov.in/others-helplines/",
        "https://pib.gov.in/PressReleaseIframePage.aspx?PRID=2100706",
        "https://mpowerminds.com/oneonone",
        "https://www.who.int/india/health-topics/mental-health",
        # Add more relevant URLs as needed
    ]

    logger.info("Loading documents from URLs...")
    # Load documents from the URLs
    docs = []
    for url in urls:
        try:
            loader = WebBaseLoader(url)
            docs.extend(loader.load())
        except Exception as e:
            logger.error(f"Error loading from {url}: {str(e)}")

    # Split documents into manageable chunks
    text_splitter = CharacterTextSplitter.from_tiktoken_encoder(chunk_size=7500, chunk_overlap=100)
    doc_splits = text_splitter.split_documents(docs)

    logger.info("Creating embeddings and vector store...")
    # Create embeddings with GPU support
    embedding_model = OllamaEmbeddings(
        model='nomic-embed-text',
        base_url="http://localhost:11434"
    )
    vectorstore = Chroma.from_documents(
        documents=doc_splits,
        collection_name="mental-health-india",
        embedding=embedding_model,
    )
    retriever = vectorstore.as_retriever()

    # Initialize conversation memory
    memory = ConversationBufferMemory()

    # Define the prompt template for retrieval-augmented generation (RAG)
    after_rag_template = """Answer the following question based only on the context provided below.
    If the answer is not contained within the context, respond with "I am sorry, I do not have that information."

    Context:
    {context}

    Question: {question}"""

    after_rag_prompt = ChatPromptTemplate.from_template(after_rag_template)

    # Create the RAG chain with async support
    after_rag_chain = (
        {"context": retriever, "question": RunnablePassthrough()}
        | after_rag_prompt
        | model_local
        | StrOutputParser()
    )

    logger.info("RAG system initialized successfully!")

except Exception as e:
    logger.error(f"Error initializing RAG system: {str(e)}", exc_info=True)
    raise

# Example usage
if __name__ == "__main__":
    try:
        # Disable unnecessary logging
        logging.getLogger("httpx").setLevel(logging.WARNING)
        logging.getLogger("chromadb").setLevel(logging.WARNING)
        
        # Test question
        question = "What are the signs of depression?"
        print(f"\nProcessing question: {question}")
        
        # Step 1
        print("\nStep 1: Getting context...")
        context = retriever.get_relevant_documents(question)
        print("Done.")
        
        # Step 2
        print("\nStep 2: Generating response...")
        answer = after_rag_chain.invoke(question)
        print("Done.")
        
        # Show result
        print("\nAnswer:", answer)
        
    except Exception as e:
        logger.error(f"Error: {str(e)}")
