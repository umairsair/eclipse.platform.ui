/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.search.internal.ui.text;

import java.util.regex.Pattern;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;

import org.eclipse.search.ui.ISearchQuery;
import org.eclipse.search.ui.ISearchResult;
import org.eclipse.search.ui.text.AbstractTextSearchResult;
import org.eclipse.search.ui.text.FileTextSearchScope;

import org.eclipse.search.core.text.TextSearchEngine;
import org.eclipse.search.core.text.TextSearchMatchAccess;
import org.eclipse.search.core.text.TextSearchRequestor;

import org.eclipse.search.internal.core.text.FileNamePatternSearchScope;
import org.eclipse.search.internal.core.text.PatternConstructor;
import org.eclipse.search.internal.ui.Messages;
import org.eclipse.search.internal.ui.SearchMessages;


public class FileSearchQuery implements ISearchQuery {
	
	private final static class TextSearchResultCollector extends TextSearchRequestor {
		
		private final AbstractTextSearchResult fResult;
		private final boolean fIsFileSearchOnly;
		
		private TextSearchResultCollector(AbstractTextSearchResult result, boolean isFileSearchOnly) {
			fResult= result;
			fIsFileSearchOnly= isFileSearchOnly;
		}
		
		public boolean acceptFile(IFile file) throws CoreException {
			if (fIsFileSearchOnly) {
				fResult.addMatch(new FileMatch(file, 0, 0));
			}
			return true;
		}

		public boolean acceptPatternMatch(TextSearchMatchAccess matchRequestor) throws CoreException {
			fResult.addMatch(new FileMatch(matchRequestor.getFile(), matchRequestor.getMatchOffset(), matchRequestor.getMatchLength()));
			return true;
		}

	}
	
	private final FileTextSearchScope fScope;
	private final String fSearchText;
	private final boolean fIsRegEx;
	private final boolean fIsCaseSensitive;
	
	private FileSearchResult fResult;
	
	public FileSearchQuery(String searchText, boolean isRegEx, boolean isCaseSensitive, FileTextSearchScope scope) {
		fSearchText= searchText;
		fIsRegEx= isRegEx;
		fIsCaseSensitive= isCaseSensitive;
		fScope= scope;
	}
	
	public FileTextSearchScope getSearchScope() {
		return fScope;
	}
	
	public boolean canRunInBackground() {
		return true;
	}

	public IStatus run(final IProgressMonitor monitor) {
		AbstractTextSearchResult textResult= (AbstractTextSearchResult) getSearchResult();
		textResult.removeAll();
		
		Pattern searchPattern= getSearchPattern();
		boolean isFileSearchOnly= searchPattern.pattern().length() == 0;
		
		TextSearchResultCollector collector= new TextSearchResultCollector(textResult, isFileSearchOnly);
		return TextSearchEngine.create().search(fScope, collector, searchPattern, monitor);
	}

	public String getLabel() {
		return SearchMessages.FileSearchQuery_label; 
	}
	
	public String getSearchString() {
		return fSearchText;
	}
	
	public String getResultLabel(int nMatches) {
		String searchString= getSearchString();
		if (nMatches == 1) {
			if (searchString.length() > 0) {
				Object[] args= { searchString, fScope.getDescription() };
				return Messages.format(SearchMessages.FileSearchQuery_singularLabel, args); 
			}
			Object[] args= { fScope.getFilterDescription(), fScope.getDescription() };
			return Messages.format(SearchMessages.FileSearchQuery_singularLabel_fileNameSearch, args); 
		}
		if (searchString.length() > 0) {
			Object[] args= { searchString, new Integer(nMatches), fScope.getDescription() };
			return Messages.format(SearchMessages.FileSearchQuery_pluralPattern, args); 
		}
		Object[] args= { fScope.getFilterDescription(), new Integer(nMatches), fScope.getDescription() };
		return Messages.format(SearchMessages.FileSearchQuery_pluralPattern_fileNameSearch, args); 
	}

	/**
	 * @param result all result are added to this search result
	 * @param monitor the progress monitor to use
	 * @param file the file to search in
	 * @return returns the status of the operation
	 */
	public IStatus searchInFile(final AbstractTextSearchResult result, final IProgressMonitor monitor, IFile file) {
		FileNamePatternSearchScope scope= FileNamePatternSearchScope.newSearchScope("", new IResource[] { file }, true); //$NON-NLS-1$
		
		Pattern searchPattern= getSearchPattern();
		boolean isFileSearchOnly= searchPattern.pattern().length() == 0;
		TextSearchResultCollector collector= new TextSearchResultCollector(result, isFileSearchOnly);
		
		return TextSearchEngine.create().search(scope, collector, searchPattern, monitor);
	}
	
	protected Pattern getSearchPattern() {
		return PatternConstructor.createPattern(fSearchText, fIsCaseSensitive, fIsRegEx);
	}
	
	public boolean isRegexSearch() {
		return fIsRegEx;
	}
	
	public boolean isCaseSensitive() {
		return fIsCaseSensitive;
	}

	public boolean canRerun() {
		return true;
	}

	public ISearchResult getSearchResult() {
		if (fResult == null) {
			fResult= new FileSearchResult(this);
			new SearchResultUpdater(fResult);
		}
		return fResult;
	}
}
