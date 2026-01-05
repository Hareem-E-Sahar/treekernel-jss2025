import torch
import torch.nn.functional as F
from transformers import AutoTokenizer, AutoModel
import os, re
from readfiles import load_java_files, remove_comments, read_method_from_file
from groundtruth import *

tokenizer = AutoTokenizer.from_pretrained("microsoft/codebert-base")
model     = AutoModel.from_pretrained("microsoft/codebert-base")
model.eval()

"""
code_snippet = "def add(a, b): return a + b"
#code_snippet = "public int add(int a, int b) { return a + b; }"
tokens = tokenizer(code_snippet, return_tensors="pt")
outputs = model(**tokens)
embedding = outputs.last_hidden_state.mean(dim=1)  # mean pooling
print(embedding.shape) #torch.Size([1, 768])
"""

def embed_code(code_snippet):
    inputs = tokenizer(code_snippet, return_tensors="pt", truncation=True, max_length=512)
    with torch.no_grad():
        outputs = model(**inputs)
        #embedding = outputs.last_hidden_state[:,0,:]  # CLS token
        embedding = outputs.last_hidden_state.mean(dim=1)  # mean pooling
        # Normalize for cosine similarity or retrieval
        embedding = F.normalize(embedding, dim=1)
    return embedding.squeeze(0)

    
def get_similarity_matrix(query_embeddings, corpus_embeddings):
    """
    Compute cosine similarity matrix between queries and corpus.

    Args:
        query_embeddings (Tensor): [num_queries, D]
        corpus_embeddings (Tensor): [num_corpus, D]

    Returns:
        similarity_matrix (Tensor): [num_queries, num_corpus]
    """
    query_norm = F.normalize(query_embeddings, dim=1)
    corpus_norm = F.normalize(corpus_embeddings, dim=1)
    similarity_matrix = torch.matmul(query_norm, corpus_norm.T)
    return similarity_matrix

def get_similarity_matrix2():
    # not used
    query_norm  = F.normalize(query_embeddings, dim=1)
    corpus_norm = F.normalize(corpus_embeddings, dim=1)
  
    # similarity matrix [num_queries, num_corpus]
    similarity_matrix = query_norm @ corpus_norm.T
    print(similarity_matrix.shape)  # [100, 50000]

    topk = 10
    for i, q_file in enumerate(query_files):
        topk_vals, topk_idx = similarity_matrix[i].topk(topk)
        topk_methods = [method_ids[j] for j in topk_idx]
        print(f"Query: {q_file}")
        print("Top matches:")
        for rank, (method, score) in enumerate(zip(topk_methods, topk_vals), 1):
            print(f"{rank}. {method}  (score={score.item():.4f})")
        print()


def get_topk_matches(similarity_matrix, method_ids, file_to_idx, query_files, K=10, return_indices=False):
    """
    Get top-k matches for each query from a similarity matrix.

    Args:
        similarity_matrix (Tensor): [num_queries, num_corpus]
        method_ids (list of str): corpus method identifiers
        query_files (list of str): query method identifiers
        K (int): number of top matches
        return_indices (bool): if True, also return corpus indices of top-k

    Returns:
        results (dict): {query_file: [(method, score), ...]}
        (optional) indices (dict): {query_file: [corpus_idx1, corpus_idx2, ...]}
    """
    topk_vals, topk_idx = similarity_matrix.topk(K, dim=1)
    results = {}
    indices = {} if return_indices else None
    #file_to_idx = {os.path.basename(f): i for i, f in enumerate(method_ids)}


    for i, q_file in enumerate(query_files):
        results[q_file] = [(method_ids[j], topk_vals[i, idx].item()) 
                           for idx, j in enumerate(topk_idx[i])]
        if return_indices:
            indices[q_file] = topk_idx[i].tolist()
    
    """
    for i, q_file in enumerate(query_files):
        results[q_file] = []  # for this query
    
        # iterate through each top-k index for this query
        for idx, j in enumerate(topk_idx[i]):
            method = method_ids[j]            # get the candidate method
            score = topk_vals[i, idx].item()  # similarity score
            results[q_file].append((method, score))
   
        if return_indices:
            indices[q_file] = topk_idx[i].tolist()
    """

    if return_indices:
        return results, indices
    else:
        return results


def get_ranked_matches(similarity_matrix, method_ids, query_files, return_indices=False):
    """
    Always return full ranking for each query.
    """
    sorted_vals, sorted_idx = similarity_matrix.sort(dim=1, descending=True)

    results = {}
    indices = {} if return_indices else None

    for i, q_file in enumerate(query_files):
        results[q_file] = [
            (method_ids[j], sorted_vals[i, rank].item())
            for rank, j in enumerate(sorted_idx[i])
        ]
        if return_indices:
            indices[q_file] = sorted_idx[i].tolist()

    return (results, indices) if return_indices else results

