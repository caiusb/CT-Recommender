/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package edu.illinois.codingtracker.tests.postprocessors.ast.refactoring.properties;



/**
 * @author Stas Negara
 * 
 */
public class ChangedGlobalEntityNameInUsageRefactoringProperty extends ChangedEntityNameInUsageRefactoringProperty {

	public ChangedGlobalEntityNameInUsageRefactoringProperty(String oldEntityName, String newEntityName, long entityNameNodeID, String sourceMethodName, long activationTimestamp) {
		super(oldEntityName, newEntityName, entityNameNodeID, sourceMethodName, activationTimestamp);
	}

}
