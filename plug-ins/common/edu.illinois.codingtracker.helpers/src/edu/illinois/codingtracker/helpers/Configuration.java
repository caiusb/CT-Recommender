/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package edu.illinois.codingtracker.helpers;


/**
 * 
 * @author Stas Negara
 * 
 */
public class Configuration {

	public static final boolean isInDebugMode= true;//System.getenv("DEBUG_MODE") != null;

	public static final boolean isInTestMode= System.getenv("TEST_MODE") != null;

	public static final boolean isOldFormat= System.getenv("OLD_CODINGTRACKER_FORMAT") != null;

	public static final boolean isOldASTFormat= System.getenv("OLD_AST_CODINGTRACKER_FORMAT") != null;

	public static final String postprocessorRootFolderName= "/Users/caius/osu/TransformationRecommender/play/test-data"; //System.getenv("POSTPROCESSOR_ROOT_FOLDER");

	public static final long usageTimeStart;

	public static final long usageTimeStop;

	public static final boolean isInPostprocessMode= true;

	public static final boolean isInASTInferenceMode= true; //System.getenv("AST_INFERENCE_MODE") != null;

	public static final boolean isInRefactoringInferenceMode= false; //System.getenv("REFACTORING_INFERENCE_MODE") != null;

	public static final boolean isInReplayMode= true; //System.getenv("REPLAY_MODE") != null;

	public static final int miningFrequencyTimesSizeThreshold;

	public static final int miningMinimumItemsetFrequency = 1;

	public static final long miningMaxBlockSize;

	public static final long miningMaxOutputItemSetsCount;

	public static final int minimumSizeThreshold = 20;

	public static final String TRANSFORMATION_KINDS_FILE = "transformationKinds.csv";
	
	public static final String ATOMIC_TRANSFORMATIONS_FILE = "atomicTransformations.txt";
	
	public static final String TRAINING_DATA_FOLDER = "/Users/caius/osu/TransformationRecommender/play/training-data";
	
	public static final String MINING_RESULTS_FOLDER = "MiningResults";

	public static final String ITEM_SETS_FOLDER = "MiningResults/Size";

	static {
		String envUsageTimeStart= System.getenv("USAGE_TIME_START");
		if (envUsageTimeStart != null) {
			usageTimeStart= Long.parseLong(envUsageTimeStart);
		} else {
			usageTimeStart= Long.MIN_VALUE;
		}

		String envUsageTimeStop= System.getenv("USAGE_TIME_STOP");
		if (envUsageTimeStop != null) {
			usageTimeStop= Long.parseLong(envUsageTimeStop);
		} else {
			usageTimeStop= Long.MAX_VALUE;
		}

		String envFrequencyThreshold= System.getenv("MINING_FREQUENCY_TIMES_SIZE_THRESHOLD");
		if (envFrequencyThreshold != null) {
			miningFrequencyTimesSizeThreshold= Integer.parseInt(envFrequencyThreshold);
		} else {
			miningFrequencyTimesSizeThreshold= 1;
		}

//		String envMinimumFrequency= System.getenv("MINING_MINIMUM_ITEMSET_FREQUENCY");
//		if (envMinimumFrequency != null) {
//			miningMinimumItemsetFrequency= Integer.parseInt(envMinimumFrequency);
//		} else {
//			miningMinimumItemsetFrequency= 1;
//		}

		String envMiningMaxBlockSize= System.getenv("MINING_MAX_BLOCK_SIZE"); //Is defined in minutes.
		if (envMiningMaxBlockSize != null) {
			miningMaxBlockSize= Integer.parseInt(envMiningMaxBlockSize) * 60 * 1000;
		} else {
			miningMaxBlockSize= 5 * 60 * 1000; //5 minutes in milliseconds.
		}

		String envMiningMaxOutputItemSetsCount= System.getenv("MINING_MAX_OUTPUT_ITEM_SETS_COUNT");
		if (envMiningMaxOutputItemSetsCount != null) {
			miningMaxOutputItemSetsCount= Integer.parseInt(envMiningMaxOutputItemSetsCount);
		} else {
			miningMaxOutputItemSetsCount= Integer.MAX_VALUE;
		}
	}
}
