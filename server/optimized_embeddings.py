import logging
import torch
from langchain_community.embeddings import HuggingFaceEmbeddings
from langchain_community.vectorstores import Chroma
from langchain_core.documents import Document

class OptimizedEmbeddings:
    """A class for efficiently handling embeddings with GPU acceleration if available"""
    
    def __init__(self, persist_directory="./chroma_db", embedding_model="nomic-embed-text:latest"):
        self.logger = logging.getLogger("OptimizedEmbeddings")
        self.persist_directory = persist_directory
        self.embedding_model = embedding_model
        self._embeddings = None
        self._collections = {}
        
        # Check for GPU
        self.has_gpu = torch.cuda.is_available()
        if self.has_gpu:
            self.logger.info(f"GPU detected: {torch.cuda.get_device_name(0)}")
        else:
            self.logger.info("No GPU detected, using CPU only")
    
    def _get_embeddings(self):
        """Lazy loading of embeddings"""
        if self._embeddings is None:
            self.logger.info(f"Initializing embeddings with model: {self.embedding_model}")
            try:
                self._embeddings = HuggingFaceEmbeddings(
                    model_name=self.embedding_model, 
                    model_kwargs={'device': 'cuda' if self.has_gpu else 'cpu'}
                )
                self.logger.info("Embeddings initialized successfully")
            except Exception as e:
                self.logger.error(f"Error initializing embeddings: {str(e)}")
                # Fall back to a simpler model if the main one fails
                try:
                    self.logger.info("Falling back to all-MiniLM-L6-v2 model")
                    self._embeddings = HuggingFaceEmbeddings(
                        model_name="all-MiniLM-L6-v2",
                        model_kwargs={'device': 'cuda' if self.has_gpu else 'cpu'}
                    )
                    self.logger.info("Fallback embeddings initialized successfully")
                except Exception as e2:
                    self.logger.error(f"Error initializing fallback embeddings: {str(e2)}")
                    raise
        return self._embeddings
    
    def _get_collection(self, collection_name):
        """Get or create a Chroma collection"""
        if collection_name not in self._collections:
            self.logger.info(f"Initializing collection: {collection_name}")
            embeddings = self._get_embeddings()
            self._collections[collection_name] = Chroma(
                collection_name=collection_name,
                embedding_function=embeddings,
                persist_directory=self.persist_directory
            )
        return self._collections[collection_name]
    
    def add_texts(self, texts, collection_name="default", metadatas=None):
        """Add texts to the vector store"""
        if not texts:
            return []
        
        try:
            collection = self._get_collection(collection_name)
            return collection.add_texts(texts, metadatas=metadatas)
        except Exception as e:
            self.logger.error(f"Error adding texts to collection {collection_name}: {str(e)}")
            raise
    
    def similarity_search(self, query, collection_name="default", k=4):
        """Find similar documents"""
        if not query:
            return []
        
        try:
            collection = self._get_collection(collection_name)
            return collection.similarity_search(query, k=k)
        except Exception as e:
            self.logger.error(f"Error in similarity search for collection {collection_name}: {str(e)}")
            return []
    
    def status(self):
        """Return the status of the embeddings system"""
        return {
            "initialized": self._embeddings is not None,
            "collections": list(self._collections.keys()),
            "gpu_available": self.has_gpu
        } 