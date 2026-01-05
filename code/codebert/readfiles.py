import os,re
import pandas as pd
base_dir = os.path.expanduser('~/treekernel-emse2025/data/BigCloneEval/ijadataset/functionStr/0')

def load_java_files(base_dir):
    java_files = []
    for subdir in ["default", "sample", "selected"]:
        subpath = os.path.join(base_dir, subdir)
        for root, _, files in os.walk(subpath):
            for f in files:
                if f.endswith(".java"):
                    java_files.append(os.path.join(root, f))
    return java_files
    
def remove_comments(code):
    code = re.sub(r"//.*?\n|/\*.*?\*/", "", code, flags=re.DOTALL)
    return code
    
def read_method_from_file(java_file):
    with open(java_file, "r", encoding="utf-8", errors="ignore") as f:
        code = f.read()
    return code


#for RQ3
def read_complexity_data(complexity_file):
    df = pd.read_csv(complexity_file)
    print(df.head())

    # filter rows where Cyclomatic Complexity > 10
    # Make sure the column is numeric
    df["Cyclomatic Complexity"] = pd.to_numeric(df["Cyclomatic Complexity"], errors='coerce')
    high_complexity_df = df[df["Cyclomatic Complexity"] > 10]
    high_complexity_files = high_complexity_df["File Name"].tolist()
    #print("High Complexity Files:", high_complexity_files)
    print(len(high_complexity_files))

    low_complexity_df  = df[df["Cyclomatic Complexity"] <= 10]
    low_complexity_files = low_complexity_df["File Name"].tolist()
    #print("Low Complexity Files:", low_complexity_files)
    print(len(low_complexity_files))
    return low_complexity_files, high_complexity_files


