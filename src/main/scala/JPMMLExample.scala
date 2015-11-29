import java.io.File
import java.io.FileInputStream

import org.dmg.pmml.PMML

import org.jpmml.evaluator.Evaluator
import org.jpmml.evaluator.ModelEvaluatorFactory
import org.jpmml.model.ImportFilter
import org.jpmml.model.JAXBUtil
import org.xml.sax.InputSource

import collection.JavaConversions._

import com.github.tototoshi.csv._

object JPMMLExample {
  def main(args: Array[String]) {
    // Set up the PMML evaluator
    val pmml_filepath = "data/single_iris_logreg.xml"
    val evaluator = prepModelEvaluator(readPMML(pmml_filepath))
    
    // Read the CSV file
    val csv_filename = "data/Iris.csv"
    val csv_list = readCSV(csv_filename)
    
    // Prepare predictor fields
    val scoring_list = csv_list.map(row => evaluator.getActiveFields.map(field => (field -> evaluator.prepare(field, (row.get(field.toString).get)))).toMap)
    
    // Perform scoring using JPMML
    val regular_score_list = scoring_list.map(x => evaluator.evaluate(x).values.toList)
    
    // Save the outputs as a CSV
    val regular_output_filename = "data/Iris_scored.csv"
    writeCSV(regular_output_filename, regular_score_list)
  }
  
  // Reads the PMML File and returns a PMML object
  def readPMML(filename: String): PMML = {
    val is = new FileInputStream(new File(filename))
    try {
			val source = ImportFilter.apply(new InputSource(is))
			return JAXBUtil.unmarshalPMML(source)
		} finally {
			is.close();
		}
  }
  
  // Prepares the model evaluator
  // (note that it returns Evaluator and not ModelEvaluator)
  def prepModelEvaluator(pmml: PMML): Evaluator = {
    return ModelEvaluatorFactory.newInstance().newModelManager(pmml)
  }
  
  // Reads CSV with headers
  def readCSV(filename: String): List[Map[String,String]] = {
    val reader = CSVReader.open(new File(filename))
    return reader.allWithHeaders()
  }
  
  // Write a CSV file
  def writeCSV(filename: String, output_list: List[List[Any]]) = {
    val f = new File(filename)
    val writer = CSVWriter.open(f)
    writer.writeAll(output_list)
    writer.close
  }
}