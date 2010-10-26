import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.StringTokenizer;

import jing.chemParser.ChemParser;

public class CheckForwardAndReverseRateCoefficients {
	
	// Assumes less than 401 species are present in the mechanism
	static String[] names = new String[400];
	static double[][] coeffs = new double[400][14];
	static int numR = 0;
	static int numP = 0;
	
	public static void main(String args[]) {
		// Temperature is assumed to have units in Kelvin
		double[] T = new double [10];
		for (int i=0; i<10; i++) {
			double temp = (1000.0/3000.0) + ((double)i/9.0) * (1000.0/300.0 - 1000.0/3000.0);
			T[i] = 1000.0 / temp;
		}
		
//		double[] T = {614.0,614.0,614.0,614.0,614.0,629.0,643.0,678.0,713.0,749.0,784.0,854.0,930.0,1010.0,1100.0,1250.0,1350.0,1440.0,1510.0,1570.0,1640.0,1790.0,1970.0,2030.0,2090.0,2110.0,2130.0,2170.0,2180.0,2210.0,2220.0,2220.0,2220.0,2220.0,2210.0,2210.0,2210.0,2200.0,2190.0,2180.0,2170.0,2160.0,2150.0,2150.0,2140.0,2140.0,2140.0,2140.0,2130.0,2130.0,2130.0,2120.0,2120.0,2120.0,2120.0,2110.0,2110.0,2110.0,2110.0,2110.0,2100.0,2100.0,2100.0,2090.0,2080.0,2080.0,2070.0,2060.0,2050.0,2050.0,2040.0,2030.0,2030.0,2020.0,2010.0,2000.0,2000.0,1990.0,1980.0,1970.0,1970.0,1960.0,1950.0,1950.0,1950.0,1950.0,1950.0,1950.0,1950.0,1950.0,1950.0,1950.0,1950.0,1950.0,1950.0,1950.0,1950.0,1950.0,1950.0,1950.0,1950.0,1950.0,1950.0,1950.0,1950.0,1950.0,1950.0,1950.0,1950.0,1950.0,1950.0,1950.0,1950.0,1950.0,1950.0,1950.0,1950.0,1950.0};
//		double[] T = {681.0,681.0,681.0,681.0,681.0,681.0,681.0,681.0,681.0,681.0,681.0,681.0,681.0,681.0,681.0,681.0,681.0,681.0,681.0,681.0,681.0,681.0,681.0,681.0,681.0,681.0,681.0,681.0,681.0,681.0,681.0,681.0,681.0,681.0,681.0,681.0,681.0,681.0,681.0,682.0,683.0,684.0,687.0,693.0,703.0,712.0,730.0,749.0,767.0,786.0,823.0,860.0,897.0,934.0,1000.0,1060.0,1120.0,1170.0,1230.0,1320.0,1370.0,1410.0,1440.0,1480.0,1550.0,1660.0,1760.0,1850.0,1920.0,1960.0,2060.0,2110.0,2150.0,2200.0,2240.0,2260.0,2260.0,2250.0,2250.0,2240.0,2240.0,2230.0,2220.0,2210.0,2200.0,2190.0,2180.0,2180.0,2180.0,2180.0,2180.0,2180.0,2170.0,2170.0,2170.0,2170.0,2170.0,2170.0,2160.0,2160.0,2150.0,2150.0,2140.0,2140.0,2130.0,2130.0,2120.0,2120.0,2110.0,2110.0,2100.0,2100.0,2090.0,2090.0,2080.0,2080.0,2070.0,2070.0,2060.0,2050.0,2050.0,2040.0,2030.0,2030.0,2020.0,2010.0,2010.0,2000.0,2000.0,2000.0,2000.0,2000.0,2000.0,2000.0,2000.0,2000.0,2000.0,2000.0,2000.0,2000.0,2000.0,2000.0,2000.0,2000.0,2000.0,2000.0,2000.0,2000.0,2000.0,2000.0,2000.0,2000.0,2000.0,2000.0,2000.0,2000.0,2000.0,2000.0,2000.0,2000.0,2000.0,2000.0,2000.0};

//		double[] T = {2.95E+02, 2.95E+02, 2.95E+02, 2.95E+02, 6.12E+02, 9.28E+02, 1.24E+03, 1.56E+03, 1.88E+03, 2.19E+03, 2.19E+03,	2.19E+03};
		
		// Pressure is assumed to have units in atm
//		double[] Pressure = {0.03947692};
		double[] Pressure = {1};
		
		// Read in the chem.inp file
		try {
			FileReader fr_thermodat = new FileReader(args[0]);
			BufferedReader br_thermodat = new BufferedReader(fr_thermodat);
			String line = ChemParser.readMeaningfulLine(br_thermodat, true);
			
			// Continue reading in the file until "THERMO" is read in
			boolean foundThermo = false;
			while (!foundThermo) {
				line = ChemParser.readMeaningfulLine(br_thermodat, true);
				if (line.startsWith("THERMO")) {
					foundThermo = true;
					line = ChemParser.readMeaningfulLine(br_thermodat, true);	// This line contains the global Tmin, Tmax, Tmid
					line = ChemParser.readMeaningfulLine(br_thermodat, true);	// This line should have thermo (or comments)
				}
			}
			
			int counter = 0;
			// Read in all information
			while (line != null && !line.equals("END")) {
				if (!line.startsWith("!")) {
					// Thermo data for each species stored in 4 consecutive lines
					String speciesName = line.substring(0,16);
					StringTokenizer st_chemkinname = new StringTokenizer(speciesName);
					names[counter] = st_chemkinname.nextToken().trim();
//					String lowTstring = line.substring(46,56);
//					double lowT = Double.parseDouble(lowTstring.trim());
//					String highTstring = line.substring(56,66);
//					double highT = Double.parseDouble(highTstring.trim());
//					String midTstring = line.substring(66,74);
//					double midT = Double.parseDouble(midTstring.trim());
					// Read in the coefficients
					for (int numLines=0; numLines<2; ++numLines) {
						line = ChemParser.readMeaningfulLine(br_thermodat, false);
						for (int numcoeffs=0; numcoeffs<5; ++numcoeffs) {
							coeffs[counter][5*numLines+numcoeffs] = Double.parseDouble(line.substring(15*numcoeffs,15*(numcoeffs+1)).trim());
						}
					}
					line = ChemParser.readMeaningfulLine(br_thermodat, false);
					for (int numcoeffs=0; numcoeffs<4; ++numcoeffs) {
						coeffs[counter][10+numcoeffs] = Double.parseDouble(line.substring(15*numcoeffs,15*(numcoeffs+1)).trim());
					}
					++counter;
				}
				line = ChemParser.readMeaningfulLine(br_thermodat, true);
			}			
		
			// line should be END; next line should be REACTIONS
			line = ChemParser.readMeaningfulLine(br_thermodat, true);
			// Determine what units Ea is in
			StringTokenizer st = new StringTokenizer(line);
			double R = 1.987;
			while (st.hasMoreTokens()) {
				String nextToken = st.nextToken().toLowerCase();
				if (nextToken.equals("kcal/mol")) R = 1.987e-3;
				else if (nextToken.equals("kj/mol")) R = 8.314e-3;
				else if (nextToken.equals("j/mol")) R = 8.314;
			}
			// next line should have kinetic info
			line = ChemParser.readMeaningfulLine(br_thermodat, true);
			while (line != null && !line.equals("END")) {
				if (!line.startsWith("!")&& !line.toLowerCase().contains("low") && !line.toLowerCase().contains("troe") && !line.toLowerCase().contains("dup") && !line.toLowerCase().contains("plog") && !line.contains("CHEB")) {
					String rxnString = "";
					int[] reactsIndex = new int[3];
					int[] prodsIndex = new int[3];
					String shortRxnString = "";
					double A = 0.0;
					double n = 0.0;
					double E = 0.0;
					double[] logk = new double[T.length];
					boolean chebyshevRate = false;
					boolean rmgRate = false;
					// Find all Chebyshev rate coefficients
					if (line.contains("1.0E0 0.0 0.0")) {
						chebyshevRate = true;
						st = new StringTokenizer(line);
						rxnString = st.nextToken();
						shortRxnString = rxnString;
						String[] reactsANDprods = rxnString.split("=");
						// Determine the reactants
						reactsIndex = determineSpeciesIndex(reactsANDprods[0].substring(0,reactsANDprods[0].length()-4));
						numR = determineNumberOfSpecies(reactsANDprods[0].substring(0,reactsANDprods[0].length()-4));
						// Determine the products
						prodsIndex = determineSpeciesIndex(reactsANDprods[1].substring(0,reactsANDprods[1].length()-4));
						numP = determineNumberOfSpecies(reactsANDprods[1].substring(0,reactsANDprods[1].length()-4));
						line = ChemParser.readUncommentLine(br_thermodat);	// TCHEB & PCHEB info
						while (line.startsWith("!")) {
							line = ChemParser.readUncommentLine(br_thermodat);
						}
						StringTokenizer st_cheb = new StringTokenizer(line,"/");
						String currentToken = st_cheb.nextToken(); // TCHEB
						StringTokenizer st_values = new StringTokenizer(st_cheb.nextToken());
						double Tmin = Double.parseDouble(st_values.nextToken());
						double Tmax = Double.parseDouble(st_values.nextToken());
						double[] Ttilda = computeTtilda(T,Tmin,Tmax);
						currentToken = st_cheb.nextToken(); // PCHEB
						st_values = new StringTokenizer(st_cheb.nextToken());
						double Pmin = Double.parseDouble(st_values.nextToken());
						double Pmax = Double.parseDouble(st_values.nextToken());
						double[] Ptilda = computePtilda(Pressure,Pmin,Pmax);
						line = ChemParser.readUncommentLine(br_thermodat);	// # of basis set info
						st_cheb = new StringTokenizer(line,"/");
						currentToken = st_cheb.nextToken();
						st_values = new StringTokenizer(st_cheb.nextToken());
						int nT = Integer.parseInt(st_values.nextToken());
						int nP = Integer.parseInt(st_values.nextToken());
						// Extract the coefficients
						double[] coeffs = new double[nT*nP];
						int coeffCounter = 0;
						while (coeffCounter < nT*nP) {
							line = ChemParser.readUncommentLine(br_thermodat);
							String[] splitSlashes = line.split("/");
							StringTokenizer st_coeffs = new StringTokenizer(splitSlashes[1]);
							while (st_coeffs.hasMoreTokens()) {
								coeffs[coeffCounter] = Double.parseDouble(st_coeffs.nextToken().trim());
								++coeffCounter;
							}
						}
						double[][] phiT = computephi(Ttilda,nT);
						double[][] phiP = computephi(Ptilda,nP);
						// Compute k(T,P)
						for (int k=0; k<T.length; k++) {
							for (int i=0; i<nT; i++) {
								for (int j=0; j<nP; j++) {
									logk[k] += coeffs[i*nP+j] * phiT[i][k] * phiP[j][0];
								}
							}
						}
						String output = "";
						for (int k=0; k<T.length; k++) {
							output += logk[k] + "\t";
						}
						System.out.println(output + rxnString);
//						for (int k=0; k<T.length; k++) {
//							if (logk[k] > 20 && numR==1) System.out.println("logkf > 20 at T=" + T[k] + "K for " + rxnString);
//							else if (logk[k] > 30) System.out.println("logkf > 30 at T=" + T[k] + "K for " + rxnString);
//						}
					}
					else if (line.contains("(+m)")) {
						shortRxnString = line;
						String[] sepStrings = line.split("\\(\\+m\\)");
						// Determine the reactants
						reactsIndex = determineSpeciesIndex(sepStrings[0]);
						// Determine the products
						prodsIndex = determineSpeciesIndex(sepStrings[1].substring(1,sepStrings[1].length()));
					}
					else if (line.contains("(+M)")) {
						shortRxnString = line;
						String[] sepStrings = line.split("\\(\\+M\\)");
						// Determine the reactants
						reactsIndex = determineSpeciesIndex(sepStrings[0]);
						// Determine the products
						prodsIndex = determineSpeciesIndex(sepStrings[1].substring(1,sepStrings[1].length()));
					}
					else if (line.contains("+m")) {
						shortRxnString = line;
						String[] sepStrings = line.split("\\+m");
						// Determine the reactants
						reactsIndex = determineSpeciesIndex(sepStrings[0]);
						// Determine the products
						prodsIndex = determineSpeciesIndex(sepStrings[1].substring(1,sepStrings[1].length()));
					}
					else if (line.contains("+M=")) {
						shortRxnString = line;
						String[] sepStrings = line.split("\\+M\\=");
						// Determine the reactants
						reactsIndex = determineSpeciesIndex(sepStrings[0]);
						// Determine the products
						StringTokenizer removeComments = new StringTokenizer(sepStrings[1]);
						String inputString = removeComments.nextToken();
						prodsIndex = determineSpeciesIndex(inputString.substring(0,inputString.length()-2));
					}
					else if (line.contains("=")) {
						rmgRate = true;
						shortRxnString = line;
						st = new StringTokenizer(line);
						shortRxnString = st.nextToken();
						A = Double.parseDouble(st.nextToken());
						n = Double.parseDouble(st.nextToken());
						E = Double.parseDouble(st.nextToken());
						String output = "";
						for (int k=0; k<T.length; k++) {
							logk[k] = Math.log10(A * Math.pow(T[k],n) * Math.exp(-E/R/T[k]));
							output += logk[k] + "\t";
						}
						System.out.println(output + shortRxnString);
//						for (int k=0; k<T.length; k++) {
//							logk[k] = Math.log10(A * Math.pow(T[k],n) * Math.exp(-E/R/T[k]));
//							if (logk[k] > 20 && numR==1) System.out.println("logkf > 20 at T=" + T[k] + "K for " + shortRxnString);
//							else if (logk[k] > 30) System.out.println("logkf > 30 at T=" + T[k] + "K for " + shortRxnString);
//						}
						String[] reactsANDprods = shortRxnString.split("=");
						// Determine the reactants
						reactsIndex = determineSpeciesIndex(reactsANDprods[0]);
						numR = determineNumberOfSpecies(reactsANDprods[0]);
						// Determine the products
						prodsIndex = determineSpeciesIndex(reactsANDprods[1]);
						numP = determineNumberOfSpecies(reactsANDprods[1]);
					}
					// Calculate G_RT
					if (rmgRate || chebyshevRate) {
						double[] logKeq = new double[T.length];
						String outputString = "";
						for (int iii=0; iii<T.length; iii++) {
							double H_RT = 0; double S_R = 0; int coeffsCounter = 0;
							double Temperature = T[iii];
							if (Temperature < 1000.0) coeffsCounter = 0;
							else coeffsCounter = -7;
							for (int numReacts=0; numReacts<numR; numReacts++) {
								H_RT -= coeffs[reactsIndex[numReacts]][coeffsCounter+7] + 
										coeffs[reactsIndex[numReacts]][coeffsCounter+8]*Temperature/2 + 
										coeffs[reactsIndex[numReacts]][coeffsCounter+9]*Temperature*Temperature/3 +
										coeffs[reactsIndex[numReacts]][coeffsCounter+10]*Temperature*Temperature*Temperature/4 +
										coeffs[reactsIndex[numReacts]][coeffsCounter+11]*Temperature*Temperature*Temperature*Temperature/5 +
										coeffs[reactsIndex[numReacts]][coeffsCounter+12]/Temperature;
								S_R -= coeffs[reactsIndex[numReacts]][coeffsCounter+7]*Math.log(Temperature) + 
										coeffs[reactsIndex[numReacts]][coeffsCounter+8]*Temperature +
										coeffs[reactsIndex[numReacts]][coeffsCounter+9]*Temperature*Temperature/2 +
										coeffs[reactsIndex[numReacts]][coeffsCounter+10]*Temperature*Temperature*Temperature/3 +
										coeffs[reactsIndex[numReacts]][coeffsCounter+11]*Temperature*Temperature*Temperature*Temperature/4 + 
										coeffs[reactsIndex[numReacts]][coeffsCounter+13];
							}
							for (int numProds=0; numProds<numP; numProds++) {
								H_RT += coeffs[prodsIndex[numProds]][coeffsCounter+7] + 
										coeffs[prodsIndex[numProds]][coeffsCounter+8]*Temperature/2 + 
										coeffs[prodsIndex[numProds]][coeffsCounter+9]*Temperature*Temperature/3 +
										coeffs[prodsIndex[numProds]][coeffsCounter+10]*Temperature*Temperature*Temperature/4 +
										coeffs[prodsIndex[numProds]][coeffsCounter+11]*Temperature*Temperature*Temperature*Temperature/5 +
										coeffs[prodsIndex[numProds]][coeffsCounter+12]/Temperature;
								S_R += coeffs[prodsIndex[numProds]][coeffsCounter+7]*Math.log(Temperature) + 
										coeffs[prodsIndex[numProds]][coeffsCounter+8]*Temperature +
										coeffs[prodsIndex[numProds]][coeffsCounter+9]*Temperature*Temperature/2 +
										coeffs[prodsIndex[numProds]][coeffsCounter+10]*Temperature*Temperature*Temperature/3 +
										coeffs[prodsIndex[numProds]][coeffsCounter+11]*Temperature*Temperature*Temperature*Temperature/4 + 
										coeffs[prodsIndex[numProds]][coeffsCounter+13];
							}
							logKeq[iii] = Math.log10(Math.exp(1))*(-H_RT + S_R) + (numP-numR)*Math.log10(1.0/82.06/Temperature);
//							if (logk[iii] - logKeq[iii] > 20 && numP==1) System.out.println("logkr > 20 at T=" + T[iii] + "K for " + shortRxnString);
//							else if (logk[iii] - logKeq[iii] > 30) System.out.println("logkr > 30 at T=" + T[iii] + "K for " + shortRxnString);
							// Check if Ea is sensible
//							if (rmgRate && iii==T.length-1) {
//								double deltaHrxn = H_RT * R * T[iii];
//								double sensible = E - deltaHrxn;
//								if (sensible < 0.0)								
//									System.out.println("Ea - deltaHrxn = " + sensible + " for " + shortRxnString);
//							}
						}
						String output = "";
						for (int iii=0; iii<T.length; iii++) {
							output += (logk[iii] - logKeq[iii]) + "\t";
						}
						System.out.println(output + shortRxnString);
					}
				}
				line = ChemParser.readMeaningfulLine(br_thermodat, true);
			}
		} catch (FileNotFoundException e) {
			System.err.println("File was not found: " + args[0] + "\n");
		}
	}
	
	public static int[] determineSpeciesIndex(String reactsORprods) {
		if (reactsORprods.startsWith(">"))
			reactsORprods = reactsORprods.substring(1,reactsORprods.length());
		int[] speciesIndex = new int[3];
		int speciesCounter = 0;
		StringTokenizer st_reacts = new StringTokenizer(reactsORprods,"+");
		while (st_reacts.hasMoreTokens()) {
			String reactantString = st_reacts.nextToken();
			boolean groupedSpecies = false;
			if (reactantString.startsWith("2")) {
				reactantString = reactantString.substring(1,reactantString.length());
				groupedSpecies = true;
				
			}
			boolean found = false;
			for (int numSpecies=0; numSpecies<names.length; numSpecies++) {
				if (reactantString.equals(names[numSpecies])) {
					speciesIndex[speciesCounter]=numSpecies;
					++speciesCounter;
					if (groupedSpecies) {
						speciesIndex[speciesCounter]=numSpecies;
						++speciesCounter;
					}
					found = true;
					break;
				}
			}
			if (!found) {
				System.err.println("Could not find thermo for species: " + reactantString);
//				System.exit(0);
			}
		}
		return speciesIndex;
	}
	
	public static int determineNumberOfSpecies(String reactsORprods) {
		StringTokenizer st_reacts = new StringTokenizer(reactsORprods,"+");
		int numSpecies = 0;
		while (st_reacts.hasMoreTokens()) {
			++numSpecies;
			String tempString = st_reacts.nextToken();
			if (tempString.startsWith("2")) ++numSpecies;
		}
		return numSpecies;
	}
	
	public static double[] computeTtilda(double[] T, double Tmin, double Tmax) {
		double[] Ttilda = new double[T.length]; 
		for (int i=0; i<T.length; i++) {
			if (T[i] > Tmax) T[i] = Tmax;
			Ttilda[i] = (2/T[i] - 1/Tmin - 1/Tmax) / (1/Tmax - 1/Tmin);
		}
		return Ttilda;
	}
	
	public static double[] computePtilda(double[] P, double Pmin, double Pmax) {
		double[] Ptilda = new double[P.length];
		for (int i=0; i<P.length; i++) {
			Ptilda[i] = (2*Math.log10(P[i]) - Math.log10(Pmin) - Math.log10(Pmax)) / (Math.log10(Pmax) - Math.log10(Pmin));
		}
		return Ptilda;
	}
	
	public static double[][] computephi(double[] argumentX, int maxCounter) {
		double[][] phi = new double[maxCounter][argumentX.length];
		for (int j=0; j<argumentX.length; j++) {
			for (int i=0; i<maxCounter; i++) {
				phi[i][j] = Math.cos(i*Math.acos(argumentX[j]));
			}
		}
		return phi;
	}
}