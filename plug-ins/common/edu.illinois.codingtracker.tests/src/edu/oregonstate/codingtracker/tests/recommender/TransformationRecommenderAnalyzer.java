package edu.oregonstate.codingtracker.tests.recommender;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.TreeSet;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.NodeFinder;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IFileEditorInput;
import org.supercsv.cellprocessor.ParseLong;
import org.supercsv.cellprocessor.ift.CellProcessor;
import org.supercsv.io.CsvListReader;
import org.supercsv.prefs.CsvPreference;

import edu.illinois.codingtracker.helpers.Configuration;
import edu.illinois.codingtracker.operations.UserOperation;
import edu.illinois.codingtracker.operations.ast.ASTOperation;
import edu.illinois.codingtracker.operations.ast.ASTOperationDescriptor.OperationKind;
import edu.illinois.codingtracker.operations.ast.CompositeNodeDescriptor;
import edu.illinois.codingtracker.operations.ast.InferredUnknownTransformationOperation;
import edu.illinois.codingtracker.operations.ast.UnknownTransformationDescriptor;
import edu.illinois.codingtracker.tests.analyzers.ast.transformation.Item;
import edu.illinois.codingtracker.tests.analyzers.ast.transformation.LongItem;
import edu.illinois.codingtracker.tests.analyzers.ast.transformation.UnknownTransformationsAnalyzer;
import edu.illinois.codingtracker.tests.analyzers.ast.transformation.helpers.OperationFilePair;
import edu.illinois.codingtracker.tests.postprocessors.ast.ASTPostprocessor;
import edu.illinois.codingtracker.tests.postprocessors.ast.transformation.UnknownTransformationDescriptorFactory;

public class TransformationRecommenderAnalyzer extends ASTPostprocessor {

	private final File transformationKindsFile = new File(Configuration.TRAINING_DATA_FOLDER,
			Configuration.TRANSFORMATION_KINDS_FILE);

	private final File atomicTransformationsFile = new File(Configuration.TRAINING_DATA_FOLDER,
			Configuration.ATOMIC_TRANSFORMATIONS_FILE);

	private final File itemSetsFolder = new File(Configuration.TRAINING_DATA_FOLDER, Configuration.ITEM_SETS_FOLDER);

	private StringBuffer stringBuffer = new StringBuffer();

	/**
	 * I parse the transformationKinds.csv file and return a new, populated map.
	 */
	private Map<Long, UnknownTransformationDescriptor> parseTransformationKindsFile() {
		CsvListReader csvReader = null;
		Map<Long, UnknownTransformationDescriptor> transformationKinds = new TreeMap<Long, UnknownTransformationDescriptor>();
		try {
			csvReader = new CsvListReader(new FileReader(transformationKindsFile), CsvPreference.STANDARD_PREFERENCE);
			List<Object> transformations;
			csvReader.getHeader(true);
			while ((transformations = csvReader.read(getTransformationKindsCSVProcessors())) != null) {
				Long transformationID = (Long) transformations.get(0);
				transformationKinds.put(transformationID, new UnknownTransformationDescriptor(transformationID,
						OperationKind.valueOf((String) transformations.get(1)), (String) transformations.get(2),
						(String) transformations.get(4), (String) transformations.get(3)));
			}
		} catch (FileNotFoundException e) {
		} catch (IOException e) {
		} finally {
			try {
				csvReader.close();
			} catch (IOException e) {
			}
		}

		return transformationKinds;
	}

	/**
	 * I return the {@link CellProcessor} to be used while parsing the
	 * transformationKinds file.
	 * 
	 * @return an array with the processors
	 */
	private CellProcessor[] getTransformationKindsCSVProcessors() {
		return new CellProcessor[] { new ParseLong(), null, null, null, null };
	}

	private Map<Long, OperationFilePair> parseAtomicTransformationsFile(
			Map<Long, UnknownTransformationDescriptor> transformationKinds) {
		TreeMap<Long, OperationFilePair> atomicTransformations = new TreeMap<Long, OperationFilePair>();
		CsvListReader reader = null;
		try {
			reader = new CsvListReader(new FileReader(atomicTransformationsFile), CsvPreference.STANDARD_PREFERENCE);
			reader.getHeader(true);
			List<Object> atomicTransformation;
			while ((atomicTransformation = reader.read(getAtomicTransformationsCSVProcessors())) != null) {
				Long transformationKindID = (Long) atomicTransformation.get(1);
				Long transformationID = (Long) atomicTransformation.get(0);
				atomicTransformations.put(transformationID, new OperationFilePair(
						new InferredUnknownTransformationOperation(transformationKindID, transformationID,
								transformationKinds.get(transformationKindID), (Long) atomicTransformation.get(2)),
						(String) atomicTransformation.get(3)));
			}
		} catch (FileNotFoundException e) {
		} catch (IOException e) {
		} finally {
			try {
				reader.close();
			} catch (IOException e) {
			}
		}
		return atomicTransformations;
	}

	private CellProcessor[] getAtomicTransformationsCSVProcessors() {
		return new CellProcessor[] { new ParseLong(), new ParseLong(), new ParseLong(), null };
	}

	private List<TreeSet<Item>> parseItemSets() {
		List<TreeSet<Item>> discoveredItemSets = new ArrayList<TreeSet<Item>>();

		File[] itemSetFiles = itemSetsFolder.listFiles();
		for (File itemSetFile : itemSetFiles) {
			TreeSet<Item> currentItemSet = new TreeSet<Item>();
			discoveredItemSets.add(currentItemSet);
			try {
				BufferedReader reader = new BufferedReader(new FileReader(itemSetFile));
				String itemSetLine = reader.readLine();
				String[] bits = itemSetLine.split(":");
				String itemSet = bits[1];
				itemSet = itemSet.substring(2, itemSet.length() - 2);
				String[] items = itemSet.split(", ");
				for (String item : items) {
					currentItemSet.add(new LongItem(Long.parseLong(item)));
				}
			} catch (FileNotFoundException e) {
			} catch (IOException e) {
			}
		}

		return discoveredItemSets;
	}

	/**
	 * By default, I postprocess everything.
	 */
	@Override
	protected boolean shouldPostprocessVersionFolder(String folderName) {
		return true;
	}

	@Override
	protected void checkPostprocessingPreconditions() {
	}

	/**
	 * I am a processor that tries to recommend changes based on the things
	 * coming in. For simplicity, I only look at the AST Changes. I already know
	 * the common transformation patterns, because they are given to me by the
	 * {@link UnknownTransformationsAnalyzer}.
	 */
	@Override
	protected List<UserOperation> postprocess(List<UserOperation> userOperations) {
		Map<Long, UnknownTransformationDescriptor> transformationKinds = parseTransformationKindsFile();
		Map<Long, OperationFilePair> atomicTransformations = parseAtomicTransformationsFile(transformationKinds);
		List<TreeSet<Item>> discoveredItemSets = parseItemSets();

		Map<Long, UnknownTransformationDescriptor> astMappedTransformationKinds = new HashMap<Long, UnknownTransformationDescriptor>();
		for (UnknownTransformationDescriptor descriptor : transformationKinds.values()) {
			Long hash = hash(descriptor);
			astMappedTransformationKinds.put(hash, descriptor);
		}

		List<CandidateTransformation> candidateTransformations = new ArrayList<CandidateTransformation>();

		List<ASTOperation> operationCache = new ArrayList<ASTOperation>();
		
		int missedNodes = 0;
		
		stringBuffer.append("Timestamp,HighestRankingTransformation,Transformation,Duration(milis)\n");
		
		for (UserOperation userOperation : userOperations) {
			if (userOperation instanceof ASTOperation) {

				ASTOperation astOperation = (ASTOperation) userOperation;
				operationCache.add(astOperation);
			} else {
				for (ASTOperation operation : operationCache) {
					long startTime = System.nanoTime();

					ASTNode affectedNode = getNodeForOperation(operation);
					if (affectedNode == null) { // can't find the affected node. Should be problematic, but I'm ignoring it for now
						System.out.println("Oops something went wrong");
						missedNodes++;
						continue;
					}

					UnknownTransformationDescriptor currentDescriptor = UnknownTransformationDescriptorFactory
							.createDescriptor(operation.getOperationKind(), affectedNode);
					UnknownTransformationDescriptor existingDescriptor = astMappedTransformationKinds
							.get(hash(currentDescriptor));

					// if I can't find a descriptor, oh well, moving on
					if (existingDescriptor == null)
						continue;

					Long transformationID = existingDescriptor.getID();

					for (TreeSet<Item> itemSet : discoveredItemSets) {
						candidateTransformations = tryAndContinueATransformation(candidateTransformations,
								transformationID);
						tryAndCreateANewTransformation(candidateTransformations, transformationID, itemSet);
					}

					addCandidatesToStringBuffer(candidateTransformations, stringBuffer);
					
					long endTime = System.nanoTime();
					
					float maxCompleteness = 0;
					CandidateTransformation mostCompletedTransformation = null;
					for (CandidateTransformation transformation : candidateTransformations) {
						float completeness = transformation.getCompleteness();
						if (completeness > maxCompleteness) {
							maxCompleteness = completeness;
							mostCompletedTransformation = transformation;
						}
					}
					
					stringBuffer.append(userOperation.getTime() + "," + 
							maxRanking +"," +
							((highestRankedTransformation != null) ? highestRankedTransformation.getTransformationInHumanTerms(transformationKinds) : "null") + "," + 
							(endTime-startTime)/1000000);
					stringBuffer.append("\n");
				}
				operationCache = new ArrayList<ASTOperation>();
				
			}
			replay(userOperation);
		}

		System.out.println("In total, I missed " + missedNodes + " nodes :(");
		return userOperations;
	}

	private void addCandidatesToStringBuffer(List<CandidateTransformation> candidateTransformations,
			StringBuffer stringBuffer) {
		Collections.sort(candidateTransformations, Collections.reverseOrder());
		stringBuffer.append(candidateTransformations.size() + "\n");
		for (CandidateTransformation candidateTransformation : candidateTransformations) {
			stringBuffer.append(candidateTransformation + "\n");
		}
		stringBuffer.append("----\n");
	}

	@SuppressWarnings("static-access")
	private ASTNode getNodeForOperation(ASTOperation operation) {
		IEditorPart currentEditor = operation.getCurrentEditor();
		IEditorInput editorInput = currentEditor.getEditorInput();
		IFile file = null;
		if (editorInput instanceof IFileEditorInput) {
			file = ((IFileEditorInput)editorInput).getFile();
		} else {
			return null;
		}
		ICompilationUnit compilationUnit = JavaCore.createCompilationUnitFrom(file);
		ASTParser parser = ASTParser.newParser(AST.JLS4);
		parser.setSource(compilationUnit);
		parser.setKind(ASTParser.K_COMPILATION_UNIT);
		ASTNode rootAST = parser.createAST(new NullProgressMonitor());
		CompositeNodeDescriptor affectedNodeDescriptor = operation.getAffectedNodeDescriptor();
		int nodeOffset = affectedNodeDescriptor.getNodeOffset();
		int nodeLength = affectedNodeDescriptor.getNodeLength();
		ASTNode affectedNode = NodeFinder.perform(rootAST, nodeOffset, nodeLength);
		return affectedNode;
	}

	private void tryAndCreateANewTransformation(List<CandidateTransformation> candidateTransformations,
			Long transformationID, TreeSet<Item> itemSet) {
		LongItem item = new LongItem(transformationID);
		if (itemSet.contains(item)) {
			CandidateTransformation candidateTransformation = new CandidateTransformation(itemSet, item);
			if (candidateTransformations.contains(candidateTransformation))
				return;
			candidateTransformations.add(candidateTransformation);
		}
	}

	private List<CandidateTransformation> tryAndContinueATransformation(
			List<CandidateTransformation> candidateTransformations, Long transformationID) {
		ArrayList<CandidateTransformation> remainingTransformations = new ArrayList<CandidateTransformation>();
		for (CandidateTransformation transformation : candidateTransformations) {
			if (transformation.continuesCandidate(new LongItem(transformationID))) {
				transformation.addItem(new LongItem(transformationID));
				remainingTransformations.add(transformation);
			}
		}
		return remainingTransformations;
	}

	private Long hash(UnknownTransformationDescriptor descriptor) {
		OperationKind operationKind = descriptor.getOperationKind();
		String affectedNodeType = descriptor.getAffectedNodeType();
		String abstractedNodeContent = descriptor.getAbstractedNodeContent();
		return hash(operationKind, affectedNodeType, abstractedNodeContent);
	}

	private long hash(OperationKind operationKind, String affectedNodeType, String abstractedNodeContent) {
		return (long) (operationKind.hashCode() * 31) ^ affectedNodeType.hashCode() ^ abstractedNodeContent.hashCode();
	}

	@Override
	protected String getRecordFileName() {
		return "codechanges.txt.inferred_ast_operations";
	}

	@Override
	protected String getResultFilePostfix() {
		return ".recommender";
	}

	@Override
	protected String getResult() {
		return stringBuffer.toString();
	}
}
