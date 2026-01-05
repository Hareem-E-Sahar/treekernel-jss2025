import random, os
from groundtruth import make_clone_map
from readfiles import read_complexity_data
from util import get_method_size

def get_positive_indices(method_ids, clone_map): # those with at least one clone
    positive_indices = []
    for i, f in enumerate(method_ids):
        if f in clone_map:                       # only keep if it has clone
            positive_indices.append(i)
    return positive_indices

def select_pos_neg_queries(method_ids, n):
    #select queries both positive and negative"
    indices = list(range(len(method_ids)))
    return select_random_queries(method_ids, indices, n)
   
def select_pos_queries_only(method_ids, clone_map, n, fixed_files=None):
    if fixed_files is not None:
        query_indices = [i for i, f in enumerate(method_ids) if f in fixed_files]
        query_files = [method_ids[i] for i in query_indices]
        print(query_files)
        return query_files, query_indices

    #select only positive queries
    positive_indices = get_positive_indices(method_ids, clone_map)
    return select_random_queries(method_ids, positive_indices, n)

def select_random_queries(method_ids, indices, n):
    #random.shuffle(indices)       # it mutates original list
    #query_indices = indices[:n]   # pick 100 positive queries
    
    """Select n random queries from given indices."""
    query_indices = random.sample(indices, min(n, len(indices)))
    query_files = [method_ids[i] for i in query_indices]
    return query_files, query_indices

# For RQ3 - Complexity


"""
def get_indices_for_complexity(method_ids, complexity_set):
    #Return indices of method_ids that are in the given complexity set.
    return [i for i, f in enumerate(method_ids) if f in complexity_set]

def select_low_complexity_queries(method_ids, low_complexity_files, n):
    #Select only from low-complexity files.
    indices = get_indices_for_complexity(method_ids, low_complexity_files)
    return select_random_queries(method_ids, indices, n)

def select_high_complexity_queries(method_ids, high_complexity_files, n):
    #Select only from high-complexity files.
    indices = get_indices_for_complexity(method_ids, high_complexity_files)
    return select_random_queries(method_ids, indices, n)
"""

def get_positive_indices_for_complexity(method_ids, complexity_set, clone_map):
    #Return indices of method_ids that are in the given complexity set.
    result = []
    for i, f in enumerate(method_ids):
        if f in complexity_set and f in clone_map:  # make sure clones exists
            result.append(i)
    return result

def select_low_complexity_positive_queries(method_ids, low_complexity_files, n, clone_map, fixed_files=None):
    #Select from low-complexity files that have clone.
    if fixed_files is not None:
        query_indices = [i for i, f in enumerate(method_ids) if f in fixed_files]
        query_files = [method_ids[i] for i in query_indices]
        print(query_files)
        return query_files, query_indices

    indices = get_positive_indices_for_complexity(method_ids, low_complexity_files, clone_map)
    return select_random_queries(method_ids, indices, n)

def select_high_complexity_positive_queries(method_ids, high_complexity_files, n, clone_map, fixed_files=None):
    #Select only from high-complexity files that have clones.
    if fixed_files is not None:
        query_indices = [i for i, f in enumerate(method_ids) if f in fixed_files]
        query_files = [method_ids[i] for i in query_indices]
        print(query_files)
        return query_files, query_indices

    indices = get_positive_indices_for_complexity(method_ids, high_complexity_files, clone_map)
    return select_random_queries(method_ids, indices, n)

# RQ3 - size
"""
def get_short_method_indices(method_ids, max_loc=6):
    #Return indices of methods with fewer than max_loc lines of code.
    short_indices = []
    for i, f in enumerate(method_ids):
        loc = get_method_size(f)
        if loc < max_loc:
            short_indices.append(i)
    return short_indices

def select_short_method_queries(method_ids, max_loc=6, n=100):
    #Select up to n methods with < max_loc lines of code.
    #Returns (query_files, query_indices).
    indices = get_short_method_indices(method_ids, max_loc)
    if not indices:
        return [], []
    query_indices = random.sample(indices, min(n, len(indices)))
    query_files = [method_ids[i] for i in query_indices]
    return query_files, query_indices

def get_long_method_indices(method_ids, min_loc=10):
    #Return indices of methods with > min_loc.
    
    long_indices = []
    for i, f in enumerate(method_ids):
        loc = get_method_size(f)
        if loc > min_loc:
            long_indices.append(i)
    return long_indices

def select_long_method_queries(method_ids, min_loc=10, n=100):
    #Select up to n methods with > min_loc.
    #Returns (query_files, query_indices).
    
    indices = get_long_method_indices(method_ids, min_loc)
    if not indices:
        return [], []
    query_indices = random.sample(indices, min(n, len(indices)))     
    query_files = [method_ids[i] for i in query_indices]
    return query_files, query_indices
"""                             

def get_positive_short_method_indices(method_ids, clone_map, max_loc):
    #Return indices of method_ids that are in the given size range
    result = []
    for i, f in enumerate(method_ids):
        if (get_method_size(f) < max_loc)  and (f in clone_map):  # make sure clones exists
            result.append(i)
    return result

def get_positive_long_method_indices(method_ids, clone_map, min_loc):
    #Return indices of method_ids that are in the given size range.
    result = []
    for i, f in enumerate(method_ids):
        if (get_method_size(f) > min_loc)  and (f in clone_map):  # make sure clones exist
            result.append(i)
    return result

def select_positive_short_method_queries(method_ids, clone_map, max_loc=6, n=100, fixed_files=None):
    
    #Select up to n methods with < max_loc and that have GT.
    #Returns (query_files, query_indices).
    
    if fixed_files is not None:
        query_indices = [i for i, f in enumerate(method_ids) if f in fixed_files]
        query_files = [method_ids[i] for i in query_indices]
        print(query_files)
        return query_files, query_indices

    indices = get_positive_short_method_indices(method_ids, clone_map, max_loc)
    if not indices:
        return [], []
    query_indices = random.sample(indices, min(n, len(indices)))
    query_files = [method_ids[i] for i in query_indices]
    return query_files, query_indices

def select_positive_long_method_queries(method_ids, clone_map, min_loc=10, n=100, fixed_files=None):
    
    #Select up to n methods with > min_loc and that have GT.
    #Returns (query_files, query_indices).
    
    if fixed_files is not None:
        query_indices = [i for i, f in enumerate(method_ids) if f in fixed_files]
        query_files = [method_ids[i] for i in query_indices]
        print(query_files)
        return query_files, query_indices

    indices = get_positive_long_method_indices(method_ids, clone_map, min_loc)
    if not indices:
        return [], []
    query_indices = random.sample(indices, min(n, len(indices)))
    query_files = [method_ids[i] for i in query_indices]
    return query_files, query_indices



