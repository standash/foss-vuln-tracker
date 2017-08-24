package it.unitn.molerat.repos.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;

import it.unitn.molerat.data.csv.InputDataPoint;
import org.apache.commons.io.FileUtils;

public final class IORoutines {
	
	private static File throwExceptionIfDoesNotExist(String path) throws FileNotFoundException {
		File javaFile = new File(path);
		if (!javaFile.exists()) {
			throw new FileNotFoundException(path);
		}
		return javaFile;
	}

	public static String readFile(String path) throws IOException {
		File javaFile = throwExceptionIfDoesNotExist(path);
		BufferedReader in = new BufferedReader(new FileReader(javaFile));
		StringBuffer buffer = new StringBuffer();
		String line = null;
		try {
			while ( (line = in.readLine()) != null) {
				buffer.append(line);
				buffer.append('\n');
			}	
		}
		finally {
			in.close();
		}		
		return buffer.toString();
	}
	
	public static void writeFile(String path, String contents) throws IOException {
		File file = null;
		FileOutputStream outStream = null;
		try {
			file = new File(path);
			outStream = new FileOutputStream(file, true);
			if (!file.exists()) {
				file.createNewFile();
			}
			byte[] bytes = contents.getBytes();
			outStream.write(bytes);
		} catch (IOException e) {
		    System.out.println("ERROR: " + e.getMessage());
			throw new IOException(e.getMessage());
		} finally {
			try {
				if (outStream != null) {
					outStream.flush();
					outStream.close();
				}
			} catch (IOException e) {
				System.out.println("ERROR: " + e.getMessage());
				throw new IOException(e.getMessage());
			}
		}
	}
	
	public static Set<String> readFileBrokenByLines(String path) throws IOException {
		File javaFile = throwExceptionIfDoesNotExist(path);
		Set<String> lines = new LinkedHashSet<String>();
		BufferedReader in = new BufferedReader(new FileReader(javaFile));
		String line = null;
		try {
			while ( (line = in.readLine()) != null) {
				lines.add(line);
			}	
		}
		finally {
			in.close();
		}		
		return lines;
	}
	
	public static void readFilesRecursively(File source, Set<String> results) {
		if (!source.exists()) {
			return;
		}
		else if (source.isDirectory()) {
			String[] files = source.list();
			for (String file : files) {
				readFilesRecursively(new File(source,file), results);
			}
		}
		else {
			try {
				String result = readFile(source.getAbsolutePath());
				results.add(result);
			} catch (IOException e) {
				System.out.println("ERROR: " + e.getMessage());
			}
		}
	}

    public static Set<InputDataPoint> readInputDataPoints(String path) throws Exception {
        Set<InputDataPoint> dataPointSet = new HashSet<>();
        File file2Parse = new File(path);
        BufferedReader reader = null;
        try {
            String line = "";
            reader = new BufferedReader(new FileReader(file2Parse));
            while ((line = reader.readLine()) != null) {
                InputDataPoint inputDataPoint = new InputDataPoint(line);
                dataPointSet.add(inputDataPoint);
            }
        } catch (Exception e) {
			System.out.println("ERROR: " + e.getMessage());
        }
        finally {
            try {
                reader.close();
            } catch (Exception e) {
				System.out.println("ERROR: " + e.getMessage());
            }
        }
        return dataPointSet;
    }

	
	public static File joinpaths(String path1, String path2) {
		return new File(path1, path2);
	}
	
	public static void mkdir(File dir) {
		if (!dir.exists()) {
			dir.mkdirs();
		}
	}
	
	public void copyFolders(File source, File destination) {
	    if (source.isDirectory() && destination.isDirectory()) {
			try {
				FileUtils.copyDirectory(source, destination);
			} catch (IOException e) {
				System.out.println("ERROR: " + e.getMessage());
			}
	    }
	}
}
