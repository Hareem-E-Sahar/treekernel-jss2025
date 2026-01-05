import pandas as pd
import os


#def make_typewise_clone_map(clone_csv):
#    col_names = ["dir1", "file1", "start1", "end1", "dir2", "file2", "start2", "end2","sim1","sim2","sim3"]
#    dtypes = {col: str for col in col_names}
#    df = pd.read_csv(clone_csv, header=None, names=col_names, dtype=dtypes)
#    typewise_clone_map = {}
#    for dir1, file1, start1, end1, dir2, file2, start2, end2, s1,s2,s3 in zip(df.dir1, df.file1, df.start1, df.end1, df.dir2, df.file2, df.start2, df.end2, df.sim1, df.sim2, df.sim3):
#        m1 = f"{file1.replace('.java','')}_{start1}_{end1}.java"
#        m2 = f"{file2.replace('.java','')}_{start2}_{end2}.java"
#        # add to clone map
#        typewise_clone_map.setdefault(m1, set()).add(m2)
#        typewise_clone_map.setdefault(m2, set()).add(m1)
#    print("Length of clone map:",len(typewise_clone_map))
#    return typewise_clone_map


def make_clone_map(clone_csv):
    col_names = ["dir1", "file1", "start1", "end1", "dir2", "file2", "start2", "end2"]
    dtypes = {col: str for col in col_names}
    #df = pd.read_csv(clone_csv, header=None, names=col_names, dtype=dtypes)
    df = pd.read_csv(clone_csv, header=None, usecols=range(len(col_names)), names=col_names, dtype=dtypes)
    
    df = df.astype(str)
    clone_map = {}

    # build the canonical filenames directly in dataframe
    for dir1, file1, start1, end1, dir2, file2, start2, end2 in zip(df.dir1, df.file1, df.start1, df.end1, df.dir2, df.file2, df.start2, df.end2):
        m1 = f"{file1.replace('.java','')}_{start1}_{end1}.java"
        m2 = f"{file2.replace('.java','')}_{start2}_{end2}.java"

        # add to clone map
        clone_map.setdefault(m1, set()).add(m2)
        clone_map.setdefault(m2, set()).add(m1)
    print("Length of clone map:",len(clone_map))
    return clone_map
    

def build_groundtruth(query_files, file_to_idx, clone_csv):
    ground_truth_eval = {}
    clone_map = make_clone_map(clone_csv)

    for f in query_files:

        clones = clone_map.get(f , [])    # all its known clones (filenames)

        gt_indices = []                       # will store indices instead of names
        for g in clones:
            if g in file_to_idx:              # only keep if corpus has it
                gt_indices.append(file_to_idx[g])

        ground_truth_eval[os.path.basename(f)] = gt_indices   
    return ground_truth_eval

    
"""

clone_csv  = os.path.expanduser("~/treekernel-emse2025/data/BigCloneEval/bigclone_groundtruth_v3/T3-clones-selected-columns.txt")

clone_map = make_clone_map(clone_csv)
key ="BubbleSort_147_163.java"
print("key:",key)
if key in clone_map:
    print("=>Found:", clone_map[key])
else:
    print("=>Not found")
    
#print("Sample query_files:", query_files[:5])
print("Sample clone_map keys:", list(clone_map.keys())[:5])
"""
