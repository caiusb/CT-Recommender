/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package edu.illinois.codingtracker.tests.postprocessors.ast.refactoring.properties;



/**
 * This class represents changing a variable's name in its declaration.
 * 
 * @author Stas Negara
 * 
 */
public class ChangedVariableNameInDeclarationRefactoringProperty extends RefactoringProperty {


	public ChangedVariableNameInDeclarationRefactoringProperty(String oldVariableName, String newVariableName) {
		addAttribute(RefactoringPropertyAttributes.OLD_VARIABLE_NAME, oldVariableName);
		addAttribute(RefactoringPropertyAttributes.NEW_VARIABLE_NAME, newVariableName);
	}

}
