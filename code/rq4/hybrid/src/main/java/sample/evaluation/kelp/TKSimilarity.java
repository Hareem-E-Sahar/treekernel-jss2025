package sample.evaluation.kelp;

import sample.evaluation.util.Util;
import java.io.File;
import java.io.IOException;
import java.util.List;
import it.uniroma2.sag.kelp.data.representation.tree.TreeRepresentation;
import it.uniroma2.sag.kelp.kernel.tree.PartialTreeKernel;
import it.uniroma2.sag.kelp.kernel.tree.SubSetTreeKernel;

public class TKSimilarity {
	SimilarityMap simMap;
	String kernel;
   
	public TKSimilarity(String kernel) {
		simMap = new SimilarityMap();
		this.kernel = kernel;
	}
	
	public SimilarityMap getSimMap() {
		return simMap;
	}
	
	public float computeNormalization(String filename1, String filename2) {
		float sim1  = (float) simMap.getSelfSimilarity(filename2) ;
		float sim2 =  (float) simMap.getSelfSimilarity(filename2);
		return (float) (Math.sqrt(sim1) * Math.sqrt(sim2));
	}

	public void computeSelfSimilarity(List<File> sexpFiles) {
		for(File file1 : sexpFiles)  {
			try {
				Float sim = computeSimilarity(file1.getAbsolutePath(), file1.getAbsolutePath());
				simMap.addSelfSimilarity(file1.getName(), sim) ;
			} catch (Exception e) {
				e.printStackTrace();
			}
		}	
	}

	public  float computeSimilarity(String qfile, String otherfile) {
		
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
