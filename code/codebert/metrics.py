import os
def precision_at_k(pred_indices, ground_truth, k):
    """
    Compute Precision@k for a single query.
    """
    hits = sum(1 for idx in pred_indices[:k] if idx in ground_truth)
    return hits / k if k > 0 else 0.0


def mean_reciprocal_rank(pred_indices, ground_truth):
    """
    Compute Reciprocal Rank (RR) for a single query.
    """
    for rank, idx in enumerate(pred_indices, 1):
        if idx in ground_truth:
            return 1.0 / rank
    return 0.0


def average_precision(pred_indices, ground_truth, k):
    """
    Compute Average Precision (AP) for a single query up to k.
    """
    hits, sum_prec = 0, 0.0
    for rank, idx in enumerate(pred_indices[:k], 1):
        if idx in ground_truth:
            hits += 1
            sum_prec += hits / rank
    #return sum_prec / hits if hits > 0 else 0.0 #incorrect 
    #return sum_prec / len(ground_truth) if len(ground_truth) > 0 else 0.0 #incorrect as it does not measure AP@k
    denom = min(len(ground_truth), k)
    return sum_prec / denom if hits > 0 else 0.0





def evaluate_metrics(all_indices, query_files, ground_truth_eval):
    """
    Compute Precision@k, MRR, and MAP for all queries.
    """
    prec5_list, prec10_list, rr_list, ap_at_k_list, ap_full_list = [], [], [], [], []
    
    for q_file in query_files:
        pred_indices = all_indices[q_file]  # full list of sorted indices  
        #print("Len pred indices:",len(pred_indices))  # = no of file i.e. 73314    
        ground_truth = ground_truth_eval.get(os.path.basename(q_file), [])
        print("Query:", q_file, "  Len GT:", len(ground_truth))
        
        prec5_list.append(precision_at_k(pred_indices, ground_truth, k=5))
        prec10_list.append(precision_at_k(pred_indices, ground_truth, k=10))
        rr_list.append(mean_reciprocal_rank(pred_indices, ground_truth))
        ap_at_k_list.append(average_precision(pred_indices, ground_truth, k=100)) #AP@100
        ap_full_list.append(average_precision(pred_indices, ground_truth, k=len(pred_indices))) #full AP

    return {
        "Precision@5": round(sum(prec5_list) / len(prec5_list), 3),
        "Precision@10": round(sum(prec10_list) / len(prec10_list), 3),
        "MRR": round(sum(rr_list) / len(rr_list), 3),
        "MAP@100": round(sum(ap_at_k_list) / len(ap_at_k_list), 3),
        "MAP": round(sum(ap_full_list) / len(ap_full_list), 3),
    }

