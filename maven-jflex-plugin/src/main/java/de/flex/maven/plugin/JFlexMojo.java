/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
 * JFlex Maven2 plugin                                                     *
 * Copyright (c) 2007       Régis Décamps <decamps@users.sf.net>           *
 *                                                                         *
 *                                                                         *
 * This program is free software; you can redistribute it and/or modify    *
 * it under the terms of the GNU General Public License. See the file      *
 * COPYRIGHT for more information.                                         *
 *                                                                         *
 * This program is distributed in the hope that it will be useful,         *
 * but WITHOUT ANY WARRANTY; without even the implied warranty of          *
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the           *
 * GNU General Public License for more details.                            *
 *                                                                         *
 * You should have received a copy of the GNU General Public License along *
 * with this program; if not, write to the Free Software Foundation, Inc., *
 * 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA                 *
 *                                                                         *
 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */
package de.flex.maven.plugin;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.project.MavenProject;

import JFlex.Main;
import JFlex.Options;

import de.flex.maven.plugin.ClassInfo;

/**
 * @goal generate
 * @phase generate-sources
 * @author Régis Décamps (decamps@users.sf.net)
 * 
 */
public class JFlexMojo extends AbstractMojo {
	/**
	 * Name of the directory where to look for jflex files by default.
	 */
	public static final String SRC_MAIN_JFLEX = "src/main/jflex";

	private Log log = getLog();

	/**
	 * @parameter expression="${project}"
	 * @required
	 */
	private MavenProject project;

	// cannot use {@value SRC_MAIN_JFLEX} because Maven site goals.html
	// is kept raw.
	/**
	 * List of grammar definitions to run the JFlex parser generator on. By
	 * default, all files in <code>src/main/java/flex</code> will be
	 * processed.
	 * @see #SRC_MAIN_JFLEX
	 * @parameter
	 */
	private File[] lexDefinitions;

	/**
	 * Name of the directory into which JFlex should generate the parser.
	 * 
	 * @parameter expression="${project.build.directory}/generated-sources/jflex"
	 */
	private File outputDirectory;

	/**
	 * Whether source code generation should be verbose.
	 * 
	 * @parameter default-value="false"
	 */
	private boolean verbose;

	/**
	 * Whether to produce graphviz .dot files for the generated automata. This
	 * feature is EXPERIMENTAL.
	 * 
	 * @parameter default-value="false"
	 */
	private boolean dot;

	/**
	 * Use external skeleton file.
	 * 
	 * @parameter
	 */
	private File skeleton;

	/**
	 * Strict JLex compatibility.
	 * 
	 * @parameter default-value="false"
	 */
	private boolean jlex;

	/**
	 * Generate java parsers from lexer definition files.
	 * 
	 * This methods is checks parameters, sets options and calls
	 * JFlex.Main.generate()
	 */
	@SuppressWarnings("unchecked")
	public void execute() throws MojoExecutionException, MojoFailureException {
		// compiling the generated source in target/generated-sources/ is
		// the whole point of this plugin compared to running the ant plugin
		project.addCompileSourceRoot(outputDirectory.getAbsolutePath());

		List<File> filesIt;
		if (lexDefinitions != null) {
			// use arguments provided in the plugin configuration
			filesIt = Arrays.asList(lexDefinitions);

			log.debug("Parsing " + lexDefinitions.length
					+ " jflex files or directories given in configuration");
		} else {
			// use default lexfiles if none provided
			log.debug("Use lexer files found in (default) " + SRC_MAIN_JFLEX);
			filesIt = new ArrayList<File>();
			File defaultDir = new File(SRC_MAIN_JFLEX);
			if (defaultDir.exists() && defaultDir.isDirectory()) {
				filesIt.add(defaultDir);
			}
		}
		//process all lexDefinitions
		Iterator<File> fileIterator = filesIt.iterator();
		while (fileIterator.hasNext()) {
			File lexDefinition = fileIterator.next();

			parseLexDefinition(lexDefinition);
		}
	}

	/**
	 * Generate java code of a parser from a lexer file.
	 * 
	 * If the {@code lexDefinition} is a directory, process all lexer files
	 * contained within.
	 * 
	 * @param lexDefinition
	 *            Lexer definiton file or directory to process.
	 * @throws MojoFailureException
	 *             if the file is not found.
	 * @throws MojoExecutionException
	 */
	@SuppressWarnings("unchecked")
	private void parseLexDefinition(File lexDefinition) throws MojoFailureException,
			MojoExecutionException {
		

		if (lexDefinition.isDirectory()) {
			// recursively process files contained within
			String[] extensions = { "jflex", "jlex", "lex", "flex" };
			log.debug("Processing lexer files found in " + lexDefinition.getAbsolutePath());
			Iterator<File> fileIterator = FileUtils.iterateFiles(lexDefinition,
					extensions, true);
			while (fileIterator.hasNext()) {
				File lexFile = fileIterator.next();
				parseLexFile(lexFile);
			}
		} 
		else {
			parseLexFile(lexDefinition);
		}
	}

	private void parseLexFile(File lexFile) throws MojoFailureException, MojoExecutionException {
		log.debug("Generationg Java code from "+lexFile.getName());
		ClassInfo classInfo = null;
		try {
			classInfo = LexSimpleAnalyzer.guessPackageAndClass(lexFile);
		} catch (FileNotFoundException e1) {
			throw new MojoFailureException(e1.getMessage());
		} catch (IOException e3) {
			classInfo = new ClassInfo();
			classInfo.className = LexSimpleAnalyzer.DEFAULT_NAME;
			classInfo.packageName = null;
		}

		checkParameters(lexFile);

		/* set destination directory */
		File generatedFile = new File(outputDirectory
				+ classInfo.getOutputFilename());

		/* Generate only if needs to */
		if (lexFile.lastModified() < generatedFile.lastModified()) {
			log.info("  "+generatedFile.getName() + " is up to date.");
			return;
		}

		/*
		 * set options. Very strange that JFlex expects this in a static way.
		 */
		Options.setDefaults();
		Options.setDir(FilenameUtils.getFullPath(generatedFile
				.getAbsoluteFile().getPath()));
		Options.dump = verbose;
		Options.verbose = verbose;
		Options.dot = dot;
		if (skeleton != null) {
			Options.setSkeleton(skeleton);
		}
		Options.jlex = jlex;

		try {
			Main.generate(lexFile);
			log.info("  generated " + outputDirectory + File.separator
					+ classInfo.getOutputFilename());
		} catch (Exception e) {
			throw new MojoExecutionException(e.getMessage());
		}		
	}

	/**
	 * Get the output directory from a package name.
	 * 
	 * @param packageName
	 * @return The concatanation of the base output with the directory following
	 *         the Java convention for packages.
	 */
	public File findDestDirectory(String packageName) {
		File destDirectory;
		if (packageName == null) {
			destDirectory = outputDirectory;
		} else {
			destDirectory = new File(outputDirectory.getAbsolutePath()
					+ File.separatorChar
					+ packageName.replace('.', File.separatorChar));
		}
		return destDirectory;
	}

	/**
	 * Check parameter lexFile.
	 * 
	 * Must not be null and file must exist.
	 * 
	 * @param lexFile
	 *            input file to check.
	 * @throws MojoExecutionException
	 *             in case of error
	 */
	private void checkParameters(File lexFile) throws MojoExecutionException {
		if (lexFile == null) {
			throw new MojoExecutionException(
					"<lexFile> is empty. Please define input file with <lexFile>input.jflex</lexFile>");
		}
		if (!lexFile.exists()) {
			throw new MojoExecutionException("Input file does not exist: "
					+ lexFile.getAbsolutePath());
		}
	}

	public File getOutputDirectory() {
		return outputDirectory;
	}

	public void setOutputDirectory(File outputDirectory) {
		this.outputDirectory = outputDirectory;
	}

	public void setLexFiles(File[] lexFiles) {
		this.lexDefinitions = lexFiles;

	}

	protected void setProject(MavenProject project) {
		this.project = project;
	}

}