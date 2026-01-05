from transformers import AutoTokenizer, AutoModel
import torch
import pickle
import os, re, random, time, argparse
import torch.nn.functional as F
from groundtruth import build_groundtruth, make_clone_map
from metrics import evaluate_metrics
from codebertsim import get_similarity_matrix, get_ranked_matches, embed_code
from readfiles import load_java_files, remove_comments, read_method_from_file, read_complexity_data
from util import save_results, save_metrics, get_method_size 
import sampler as sp

def load_selected_files(path):
    print(path)
    with open(path, "r") as f:
        return [os.path.basename(line.strip()) for line in f.readlines()]

def main():
    parser = argparse.ArgumentParser()
    parser.add_argument("--experiment", type=str, required=True,
                        choices=["rq1-pos-neg","rq1-pos","T1","T2","T3","ST3-VST3",
                                "low_complexity","high_complexity",
                                "low_complexity_positive","high_complexity_positive",
                                "small_method","large_method",
                                "small_method_positive","large_method_positive"], 
                        help="Which experiment to run")
    parser.add_argument("--repeat", type=int, default=1, help="How many times to repeat the run")
    args = parser.parse_args()
    experiment = args.experiment
    repeat = args.repeat
    start_time = time.time()
    SAVE_PATH  = "corpus_embeddings.pt"
    base_dir   = os.path.expanduser("~/treekernel-jss2025/data/BigCloneEval/ijadataset/functionStr/0")  # change path in fos
    groundtruth_dir = os.path.expanduser("~/treekernel-jss2025/data/TestH2Database")
    groundtruth_typewise = os.path.expanduser("~/treekernel-jss2025/data/BigCloneEval/bigclone_groundtruth_v3")
    clone_csv  = os.path.join(groundtruth_dir, "bigclonedb_clones_alldir_8584153.txt")
    checkstyle = os.path.expanduser("~/treekernel-jss2025/data/checkstyle_complexity_all.csv")
    metrics_file = os.path.expanduser("~/TK_embeddings/codebert-metrics.csv")
    corpus_embeddings = None
    method_ids = None
    file_to_idx = None
    clone_map = make_clone_map(clone_csv)
    
    if os.path.exists(SAVE_PATH):   
        print(SAVE_PATH, " exists")
        data = torch.load(SAVE_PATH, weights_only=True)
        corpus_embeddings = data["embeddings"]
        method_ids = data["method_ids"]
        file_to_idx = data["file_to_idx"]
    else:
        # To process the whole BigCloneBench dataset, we extract all snippet embeddings.
        # Store them in a matrix & use cosine similarity to compare all pairs
        all_java_files = load_java_files(base_dir)
        print("Loaded Java files")

        corpus_embeddings = []
        method_ids = []
        for file_path in all_java_files:
            code = read_method_from_file(file_path)
            code = remove_comments(code)
            emb = embed_code(code) # 1D tensor of size 768
            corpus_embeddings.append(emb)
            method_ids.append(os.path.basename(file_path))   
    
        #mapping of names to indices for later use
        file_to_idx = {os.path.basename(f): i for i, f in enumerate(method_ids)}        #file_to_idx[os.path.basename(f)] = i
        corpus_embeddings = torch.stack(corpus_embeddings)  # [N, D] which is no of methods X 768
        torch.save({
            "embeddings": corpus_embeddings,   # [N, D] tensor
             "method_ids": method_ids,         # list of strings
             "file_to_idx": file_to_idx,       # dict mapping
             }, "corpus_embeddings.pt")
    num_methods = corpus_embeddings.shape[0] #no of rows
    print("# of methods:", num_methods)
    embedding_dim = corpus_embeddings.shape[1] #dimension
    print("embedding dim:", embedding_dim)
    print("shape of each embedding:", corpus_embeddings[0].shape)  # single embedding = torch.Size([768])

    low_complexity_files, high_complexity_files  = read_complexity_data(checkstyle)
    
    query_files, query_indices = [], []
    seeds = [ 1375, 2802, 3501, 3389] #6251, 9080, 8241, 8828, 55, 2084,
    
    for seed in seeds:
        print(f"\n=== Run {seed} for {experiment} ===")
        if experiment == "rq1-pos":
            selected_files = load_selected_files(f"./sampled/{seed}.txt")    
            query_files, query_indices = sp.select_pos_queries_only(method_ids, clone_map, 100, selected_files)
        
        elif experiment == "T1": 
            selected_files = load_selected_files(f"./sampled/T1_{seed}.txt")
            clone_csv = os.path.join(groundtruth_typewise, "T1-clones-selected-columns.txt")
            clone_map = make_clone_map(clone_csv)
            query_files, query_indices = sp.select_pos_queries_only(method_ids, clone_map, 100, selected_files)

        elif experiment == "T2":
            selected_files = load_selected_files(f"./sampled/T2_{seed}.txt")
            clone_csv = os.path.join(groundtruth_typewise, "T2-clones-selected-columns.txt")
            clone_map = make_clone_map(clone_csv)
            query_files, query_indices = sp.select_pos_queries_only(method_ids, clone_map, 100,selected_files)

        elif experiment == "T3":
            selected_files = load_selected_files(f"./sampled/T3_{seed}.txt")
            clone_csv = os.path.join(groundtruth_typewise, "T3-clones-selected-columns.txt")
            clone_map = make_clone_map(clone_csv)
            query_files, query_indices = sp.select_pos_queries_only(method_ids, clone_map, 100,selected_files)

        elif experiment == "ST3-VST3":
            selected_files = load_selected_files(f"./sampled/T3_{seed}.txt")
            clone_csv = os.path.join(groundtruth_typewise, "ST3-VST3-clones-simtoken-selected-columns.txt")
            clone_map = make_clone_map(clone_csv)
            query_files, query_indices = sp.select_pos_queries_only(method_ids, clone_map, 100,selected_files)


        elif experiment == "low_complexity_positive": #RQ3
            selected_files = load_selected_files(f"./sampled/{seed}_Complexity_Low.txt")            
            query_files, query_indices = sp.select_low_complexity_positive_queries(method_ids, low_complexity_files, 100, clone_map, selected_files)
        
        elif experiment == "high_complexity_positive": #RQ3
            selected_files = load_selected_files(f"./sampled/{seed}_SSTK_High.txt")

            query_files, query_indices = sp.select_high_complexity_positive_queries(method_ids, high_complexity_files, 100, clone_map, selected_files)
        
        elif experiment == "small_method_positive": #RQ3
            print("short method positive") 
            selected_files = load_selected_files(f"./sampled/{seed}_Size_Small.txt")
            print(selected_files)
            query_files, query_indices  = sp.select_positive_short_method_queries(method_ids, clone_map, 6, 100, selected_files)

        elif experiment == "large_method_positive": #RQ3
            selected_files = load_selected_files(f"./sampled/{seed}_Size_Large.txt")
            query_files, query_indices = sp.select_positive_long_method_queries(method_ids, clone_map, 10, 100, selected_files)

   
            """
            elif experiment == "rq1-pos-neg":
                query_files, query_indices = sp.select_pos_neg_queries(method_ids, 100)
 
            elif experiment == "low_complexity":
                query_files, query_indices = sp.select_low_complexity_queries(method_ids, low_complexity_files, 100)

            elif experiment == "high_complexity":
                query_files, query_indices = sp.select_high_complexity_queries(method_ids, high_complexity_files, 100)
            
            elif experiment == "short_method":
                query_files, query_indices = sp.select_short_method_queries(method_ids, 6 , 100)
        
            elif experiment == "long_method":
                query_files, query_indices = sp.select_long_method_queries(method_ids, 10, 100)
            """
        

    
        # compute query embeddding and then similarity 
        query_embeddings  = corpus_embeddings[query_indices]
        with torch.no_grad():
            similarity_matrix = get_similarity_matrix(query_embeddings, corpus_embeddings)
        # or load previously computed matrix
        # similarity_matrix = torch.load("similarity_matrix.pt",weights_only=True)

        # Get top-10 matches and raw indices
        # results, indices = get_topk_matches(similarity_matrix, method_ids, file_to_idx, query_files, K=10, return_indices=True)

        # Get all matches (sorted) and raw indices
        results, indices = get_ranked_matches(similarity_matrix, method_ids, query_files, return_indices=True)
         
    
        #torch.save(similarity_matrix, "similarity_matrix.pt")
        #save_results(results,indices)

        # evaluate and compute metrics
        ground_truth_eval = build_groundtruth(query_files, file_to_idx, clone_csv)
        metrics = evaluate_metrics(indices, query_files, ground_truth_eval)
        print(metrics)
        end_time = time.time()
        print("Total duration:", end_time - start_time, "seconds")
        save_metrics(metrics, metrics_file, experiment, round(end_time - start_time))

if __name__ == "__main__":
        main()
