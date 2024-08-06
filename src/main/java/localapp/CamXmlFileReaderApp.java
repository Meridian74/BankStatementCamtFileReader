package localapp;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import localapp.config.ObjectMapperConfig;
import localapp.xmlcamtreader.AccountBalance;
import localapp.xmlcamtreader.TransactionEntry;
import localapp.xmlcamtreader.XmlCamtFileReader;

import javax.xml.xpath.*;
import java.io.File;
import java.security.InvalidParameterException;
import java.util.List;

public class CamXmlFileReaderApp {
	
	public static void main(String[] args) throws JsonProcessingException {
		ObjectMapper objectMapper = ObjectMapperConfig.getObjectMapper();
		Result result = new Result();
		
		try {
			String[] accountNums = {
					"11711111-22224455-00000000",
					"11711111-33344466"
			};
			int index = 0; // fenti tömbből melyik bankszámlaszámmal teszteljünk...
			
			XmlCamtFileReader fileReader = new XmlCamtFileReader(new File("sample.cam"));

			AccountBalance accountBalances = fillNyitoZaroEgyenleg(fileReader, accountNums[index]);
			int currentStatementIndex = accountBalances.getStatementIndex();

			result.setAccount(fileReader.getStatementID(currentStatementIndex));
			result.setDate(fileReader.getStatementDate(currentStatementIndex));
			List<TransactionEntry> entries = fileReader.getAllTransactionEntry(currentStatementIndex);
			result.setEntries(entries);
		}
		catch (RuntimeException | XPathExpressionException e) {
			System.err.println(e.getMessage());
		}
		
		String json = objectMapper.writeValueAsString(result);
		
		System.out.println("Az eredmény:");
		System.out.println("------------");
		
		System.out.println(json);
	}
	
	public static AccountBalance fillNyitoZaroEgyenleg(XmlCamtFileReader fileReader, String selectedAccountNumber) throws XPathExpressionException, RuntimeException {
		boolean hasSelectedAccountNumber = false;
		
		// a kiválaszott számlaszám ellenőrzése, hogy szerepel-e a bank által küldött .CAM file-ban
		List<String> accountNumbers = fileReader.getAccountNumbersFromFile();
		for (String number : accountNumbers) {
			number = number.replace("-", "");
			if (selectedAccountNumber.startsWith(number)) {
				hasSelectedAccountNumber = true;
				break;
			}
		}
		
		// ha a menüből kiválasztott számlaszám nincs meg, akkor dobunk egy hibát
		if (!hasSelectedAccountNumber) {
			System.err.println("A kiválaszott bankszámlaszám "
							   + selectedAccountNumber
							   + " nem szerepel a bankszámlakivonat file-jában!");
			System.err.println("Banszámlakivonatban szereplő számok: "
							   + accountNumbers);
			throw new InvalidParameterException("A kiválaszott bankszámlaszám "
												+ selectedAccountNumber
												+ " nem szerepel a bankszámlakivonat file-jában");
		}
		
		// ha megvan a számlaszám, akkor a bankszámlakivonatban az adott számlaszám nyitó és záróegyenleg betöltése
        return fileReader.getBalancesByAccountNumber(selectedAccountNumber);
	}
}