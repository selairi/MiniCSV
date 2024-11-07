import java.util.*;
import java.io.*;

//   Copyright (C) 2023 P.L. Lucas
//
//
// LICENSE: BSD
// You may use this file under the terms of the BSD license as follows:
//
// "Redistribution and use in source and binary forms, with or without
// modification, are permitted provided that the following conditions are
// met:
//   * Redistributions of source code must retain the above copyright
//     notice, this list of conditions and the following disclaimer.
//   * Redistributions in binary form must reproduce the above copyright
//     notice, this list of conditions and the following disclaimer in
//     the documentation and/or other materials provided with the
//     distribution.
//   * Neither the name of developers or companies in the above copyright and its 
//     Subsidiary(-ies) nor the names of its contributors may be used to 
//     endorse or promote products derived from this software without 
//     specific prior written permission.
//
//
// THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
// "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
// LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
// A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
// OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
// SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
// LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
// DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
// THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
// (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
// OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE."



/** Read and write CSV (comma-separated values) files. There a some functions to builds maps using a column values as key.
*/
public class MiniCSV {

  final static String CSV_DEFAULT_ENCODING = "UTF-8";

  /** The same as readCSV(fileName, "UTF-8").
   * @param fileName The name of CSV file.
   * @return List with CSV table values
   */
  public static List<List<String>> readCSV(String fileName) throws IOException, FileNotFoundException {
    return readCSV(fileName, CSV_DEFAULT_ENCODING);
  }

  /** Read the CSV fileName with given encoding, delimiter = , and quotechar = ".
   * @param fileName The name of CSV file.
   * @param encoding could be "utf-8", "latin1",...
   * @return List with CSV table values
   */
  public static List<List<String>> readCSV(String fileName, String encoding) throws IOException, FileNotFoundException {
    return readCSV(fileName, encoding, '"', ',');
  }

  /** Read the CSV fileName with given encoding.
   * @param fileName The name of CSV file.
   * @param encoding could be "utf-8", "latin1",...
   * @param quotechar is the quotechar
   * @param delimiter is the char used as delimiter
   * @return List with CSV table values
   */
  public static List<List<String>> readCSV(String fileName, String encoding, char quotechar, char delimiter) throws IOException, FileNotFoundException {
    List<List<String>> records = new ArrayList<>();
    try (BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(fileName), encoding))) {
      int nextChar = 0;
      char ch = ' ', previousChar = ' ';
      boolean quote = false;
      ArrayList<String> line = new ArrayList<String>();
      StringBuffer buffer = new StringBuffer();
      while (true) {
        nextChar = br.read();
        if(nextChar > -1) {
          previousChar = ch;
          ch = (char) nextChar;
          if(quote) {
            if(ch == quotechar) {
              if(previousChar == quotechar) {
                buffer.append(ch);
                ch = ' ';
              }
              quote = false;
            } else {
              buffer.append(ch);
            }
          } else {
            if(ch == quotechar) {
              if(previousChar == quotechar) {
                buffer.append(ch);
                ch = ' ';
              }
              quote = true;
            } else if(ch == delimiter) {
              line.add(buffer.toString());
              buffer.setLength(0);
            } else if(ch == '\n') {
              line.add(buffer.toString());
              buffer.setLength(0);
              records.add(line);
              line = new ArrayList<>();
            } else if(ch == '\r') {
              // Ignore it
            } else {
              buffer.append(ch);
            }
          }
        } else {
          if(buffer.length() > 0)
            line.add(buffer.toString());
          if(line.size() > 0) {
            records.add(line);
          }
          break;
        }
      } 
    }
    return records;
  }

  /** Writes list to Stream as CSV format.
   * @param rows List with data
   * @param output stream to write data
   * @param quotechar CSV file quote
   * @param delimiter CSV file delimiter
   */
  public static void writeCSV(List<List<String>> rows, BufferedWriter out, char quotechar, char delimiter) throws java.io.IOException {
    //out = new PrintWriter(new BufferedWriter(out));
    int maxCols = 0;
    for(List<String> row : rows)
      if(maxCols < row.size()) maxCols = row.size();
    for(List<String> row : rows) {
      for(int n = 0; n < maxCols; n++) {
        if(n < row.size()) {
          String col = row.get(n);
          boolean quoted = col.contains("" + delimiter) || col.contains(" ") || col.contains("\n");
          if(quoted) out.write(quotechar);
          col = col.replaceAll("" + quotechar, quotechar + "" + quotechar);
          out.write(col);
          if(quoted) out.write(quotechar);
        }
        if(n < (maxCols - 1))
          out.write(delimiter);
      }
      out.write("\n");
    }
  }

  /** Writes list to Stream as CSV format.
   * @param rows List with data
   * @param output stream to write data
   */
  public static void writeCSV(List<List<String>> rows, BufferedWriter out) throws java.io.IOException {
    writeCSV(rows, out, '"', ',');
  }

  /** Writes data in rows list to fileName with encoding.
   * @param rows List with data to save
   * @param fileName File name
   * @param encoding Could be "utf-8", "latin1",...
   */
  public static void writeCSV(List<List<String>> rows, String fileName, String encoding) throws FileNotFoundException,UnsupportedEncodingException, java.io.IOException {
    BufferedWriter out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(fileName), encoding));
    writeCSV(rows, out, '"', ',');
    out.close();
  }

  /** Writes data in rows list to fileName with UTF-8 encoding.
   * @param rows List with data to save
   * @param fileName File name
   */
  public static void writeCSV(List<List<String>> rows, String fileName) throws FileNotFoundException,UnsupportedEncodingException,java.io.IOException {
    writeCSV(rows, fileName, CSV_DEFAULT_ENCODING);
  }

  /** Prints rows list to stdout with CSV format
   * @param rows List with data
   */
  public static void print(List<List<String>> rows) throws java.io.IOException {
    OutputStreamWriter out = new OutputStreamWriter(System.out);
    BufferedWriter writer = new BufferedWriter(out);
    writeCSV(rows, writer, '"', ',');
    writer.close();
  }

  /** Builds a HashMap with "list" data using "idColumn" as key and key duplicates are ignored. idColumn have to be a primary key.
   * @param list List with data
   * @param idColumn Column number of key
   * @return HashMap with data.
   */
  public static HashMap<String, List<String>> listToMap(List<List<String>> list, int idColumn) {
    HashMap<String, List<String>> map = new HashMap<String, List<String>>();
    for(List<String> line : list) {
      map.put(line.get(idColumn), line);
    }
    return map;
  }

  /** Builds a HashMap with "list" data using "idColumn" as key.  All key collisions are stored.
   * @param list List with data
   * @param idColumn Column number of key
   * @return HashMap with data.
   */
  public static HashMap<String, List<List<String>>> listToMapKey(List<List<String>> list, int idColumn) {
    HashMap<String, List<List<String>>> map = new HashMap<>();
    for(List<String> line : list) {
      final String key = line.get(idColumn);
      if(map.containsKey(key)) {
        map.get(key).add(line);
      } else {
        List<List<String>> items = new ArrayList<>();
        items.add(line);
        map.put(key, items);
      }
    }
    return map;
  }

  /** Builds a HashMap with "list" data using "idColumn" as key and key duplicates are ignored. idColumn have to be a primary key.
   * @param list List with data
   * @param idColumn Column number of key
   * @param ignoreErrors Ignore NumberFormatException errors.
   * @return HashMap with data.
   */
  public static HashMap<Integer, List<String>> listToMapIntId(List<List<String>> list, int idColumn, boolean ignoreErrors) {
    HashMap<Integer, List<String>> map = new HashMap<Integer, List<String>>();
    for(List<String> line : list) {
      try {
        map.put(Integer.parseInt(line.get(idColumn)), line);
      } catch(NumberFormatException e) {
        if(!ignoreErrors) throw e;
      }
    }
    return map;
  }

  /** Builds a HashMap with "list" data using "idColumn" as key. All key collisions are stored.
   * @param list List with data
   * @param idColumn Column number of key
   * @param ignoreErrors Ignore NumberFormatException errors.
   * @return HashMap with data.
   */
  public static HashMap<Double, List<List<String>>> listToMapDoubleKey(List<List<String>> list, int idColumn, boolean ignoreErrors) {
    HashMap<Double, List<List<String>>> map = new HashMap<>();
    for(List<String> line : list) {
      try {
        String item = line.get(idColumn);
        if(ignoreErrors) item = item.replace(',', '.');
        double key = Double.parseDouble(item);
        if(map.containsKey(key)) {
          map.get(key).add(line);
        } else {
          List<List<String>> items = new ArrayList<>();
          items.add(line);
          map.put(key, items);
        }
      } catch(NumberFormatException e) {
        if(!ignoreErrors) throw e;
      }
    }
    return map;
  }

  /** Builds a HashMap with "list" data using "idColumn" as key. All key collisions are stored.
   * @param list List with data
   * @param idColumn Column number of key
   * @param ignoreErrors Ignore NumberFormatException errors.
   * @return HashMap with data.
   */
  public static HashMap<Integer, List<List<String>>> listToMapIntKey(List<List<String>> list, int idColumn, boolean ignoreErrors) {
    HashMap<Integer, List<List<String>>> map = new HashMap<>();
    for(List<String> line : list) {
      try {
        String item = line.get(idColumn);
        int key = Integer.parseInt(item);
        if(map.containsKey(key)) {
          map.get(key).add(line);
        } else {
          List<List<String>> items = new ArrayList<>();
          items.add(line);
          map.put(key, items);
        }
      } catch(NumberFormatException e) {
        if(!ignoreErrors) throw e;
      }
    }
    return map;
  }

  public static void main(String args[]) throws IOException, FileNotFoundException  {
    List<List<String>> rowsExample = new ArrayList<>();
    rowsExample.add(Arrays.asList(new String[] {"Line 1", "Example, 1", "1"}));
    rowsExample.add(Arrays.asList(new String[] {"Line 2", "Example \"2,0\"", "2"}));
    System.out.println("Before:");
    print(rowsExample);
    System.out.println("Saving...");
    writeCSV(rowsExample, "example1.csv");
    System.out.println("Saved");
    List<List<String>> rows = readCSV("example1.csv");
    // rows.remove(0); // Remove first line
    System.out.println("After");
    print(rows);
    //writeCSV(rows, new PrintWriter(System.out));
    System.out.println("Item (2,2): " + rows.get(1).get(1));
    HashMap<Integer, List<String>> map = listToMapIntId(rows, 2, true);
    System.out.println("Item with id 2: " + map.get(2));
    // Print data
    for(List<String> row : rows) {
      for(String item : row) 
        System.out.print(item + "\t");
      System.out.println();
    }
    List<List<String>> rows2 = readCSV("example1.csv", "utf-8", '"', ',');
    // Print data
    System.out.println("Reading with specific parameters");
    for(List<String> row : rows2) {
      for(String item : row) 
        System.out.print(item + "\t");
      System.out.println();
    }
  }
}
