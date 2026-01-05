package sample.evaluation.kelp;
import sample.evaluation.util.Util;
import java.io.File;
import java.io.IOException;
import java.util.List;

import it.uniroma2.sag.kelp.data.representation.tree.TreeRepresentation;
import it.uniroma2.sag.kelp.kernel.tree.PartialTreeKernel;
import it.uniroma2.sag.kelp.kernel.tree.SubTreeKernel;
import it.uniroma2.sag.kelp.kernel.tree.SubSetTreeKernel;

public class TKSimilarity {
	SimilarityMap simMap;
	String kernel;
	float[] selfSimilarities;
	
   
	public TKSimilarity(String kernel) {
		this.kernel = kernel;
		this.simMap = new SimilarityMap();
		
	}
	
	public SimilarityMap getSimMap() {
		return simMap;
	}
	
	public void findClones( List<File> sexprFiles, List<File> sampledFiles) {	
		
		int total_computations = 0;
		computeSelfSimilarity(sexprFiles);
		if (sexprFiles == null || sampledFiles == null) {
		    System.out.println("Empty directoy or not enough files.");
		    return;
		}
		System.out.println("sampledFiles.size():"+sampledFiles.size());
        
       	for (File file1: sampledFiles) {
        	   for ( File file2: sexprFiles) {
		      
		        if ( file1.equals(file2) ) {
		        	continue;
		        }
		        try {
					
		        	float similarity = computeSimilarity(file1.getAbsolutePath(),file2.getAbsolutePath());
					this.simMap.addSimilarity(file1.getName(), file2.getName(), similarity);
		        	total_computations++;
		                   
		        } catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
            	}
            	System.out.println("Total:"+total_computations);
            }
    	}


	public void computeSelfSimilarity(List<File> sexpFiles) {
		for(File file1 : sexpFiles)  {
			try {
				Float sim = computeSimilarity(file1.getAbsolutePath(), file1.getAbsolutePath());
				simMap.addSelfSimilarity(file1.getName(), sim);

				// adding self similarity to the simMap will affect metrics because self ranks on top
				// simMap.addSimilarity(file1.getName(), file1.getName(), this.selfSimilarities[i]/this.selfSimilarities[i]);

			} catch (Exception e) {
				e.printStackTrace();
			}
		}	
	}
	
	
	public float computeSimilarity(String qfile, String otherfile) {
		//PartialTreeKernel treeKernel = new PartialTreeKernel(0.4f, 0.4f, 1, "tree");
		//SubTreeKernel treeKernel = new SubTreeKernel(0.4f,"tree");
		//SubSetTreeKernel treeKernel = new SubSetTreeKernel(0.4f,"tree");
		final float LAMBDA = 0.4f;
		String REPRESENTATION_ID = "tree";

		KernelWrapper treeKernel = TreeKernelFactory.createKernel(this.kernel, LAMBDA, REPRESENTATION_ID);
			
		try {
			String queryData = Util.read_sexpr_as_string(qfile);
			if (queryData == null) return -1f; //similarity cannot be computed   
			
		    TreeRepresentation t1 = new TreeRepresentation();
			t1.setDataFromText(queryData);
			
			if (qfile.equals(otherfile)) {
				return treeKernel.kernelComputation(t1,t1);
			} else {
				String otherData = Util.read_sexpr_as_string(otherfile);
			  	if (otherData == null) return -1f;
			  	TreeRepresentation t2 = new TreeRepresentation();
			  	t2.setDataFromText(otherData);
			  	return treeKernel.kernelComputation(t1, t2);
			}

		} catch (IOException e) {
			System.err.println("Error reading files: " + e.getMessage());
			return -1f;  //error reading files
		} catch (Exception e) {
			e.printStackTrace();
			return -1f;
		}
	}
	
}
