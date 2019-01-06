import java.util.*;
import java.io.*;

public class Parser {

	//Input Stack and Grammar String
	public static Stack<String> input = new Stack<>();
	public static String current_variable = "L$";

	//Current state of grammar
	public static String current_state = "";

	// Variables, Terminals, Table - Contents

	public static String[] variables = new String[] { "L", "R", "E", "Z", "C", "X", "F", "V", "T" };
	public static String[] terminals = new String[] {
		"a", "b", "c", "d", "0", "1", "2", "3", "+", "-", "*", "print", "(", ")", "$", "if"
	};

	/*	multi[y][x], multi[var][term] */
	public static String[][] table = new String[][]{
		{ "ER", "ER", "ER", "ER", "ER", "ER", "ER", "ER", null, null, null, null, "ER", null, null, null },
		{ "ER", "ER", "ER", "ER", "ER", "ER", "ER", "ER", null, null, null, null, "ER", "", "", null },
		{ "V", "V", "V", "V", "T", "T", "T", "T", null, null, null, null, "(Z", null, null, null },
		{ null, null, null, null, null, null, null, null, "F)", "F)", "F)", "F)", null, null, null, "C)" },
		{ null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, "ifEEX"},
		{ "E", "E", "E", "E", "E", "E", "E", "E", null, null, null, null, "E", "", null, null },
		{ null, null, null, null, null, null, null, null, "+L", "-L", "*L", "printL",  null, null, null, null },
		{ "a", "b", "c", "d", null, null, null, null, null, null, null, null, null, null, null, null },
		{  null, null, null, null, "0", "1", "2", "3", null, null, null, null, null, null, null, null,},
	};

	/* Master method: var, term */
	/* Gets string via terminal and variable */
	/* [IMPORTANT] When implemented, only used when metho isValid(var,term) == true */
	/* [IMPORTANT] catch array out of bounds index error, "Requested string unavailable in grammar." */
	public static String getString(String var, String term) {

		//Initialised at -1 to handle exceptions
		int x = -1;
		int y = -1;

		if (term.equals("i")) {
			x = 15;
			y = findY(var);
			return table[y][x];
		}

		if (term.equals("p")) {
			x = 11;
			y = findY(var);
			return table[y][x];
		}

		x = findX(term);
		y = findY(var);

		/* Return specified string in parse table */
		return table[y][x];
	}

	/*	Find x table value */
	public static int findX (String term) {
		int x = -1;
		for (int i=0; i<16; i++) {
			if (term.equals(terminals[i])) {
				x=i;
				break;
			}
		}
		return x;
	}
	/*	Find y table value */
	public static int findY (String var) {
		int y = -1;
		for (int j=0; j<9; j++) {
			if (var.equals(variables[j])) {
				y=j;
				break;
			}
		}
		return y;
	}


	//Checking acceptability

	/*	Find x table value */
	public static int getX(String term) {
		int x = -1;

		for (int i=0; i<16; i++) {
			if (term.equals(terminals[i])) {
				x=i;
				break;
			}
		}
		return x;
	}

	/*	Find y table value */
	public static int getY(String var) {
		int y = -1;

		for (int j=0; j<9; j++) {
			if (var.equals(variables[j])) {
				y=j;
				break;
			}
		}
		return y;
	}

	/* Checks current acceptability of input String stack */
	public static boolean validInput (String var, String term) {
		int x = getX(term);
		int y = getY(var);
		boolean isValid = false;

		if ((x>-1) && (y>-1)) {
			isValid = true;
		}

		return isValid;
	}



	/* Check if string reduction is available */
	/*[EXPLANATION] (Assumed stack is ready to go and order is right)
		1) Take first element in the input list and grammar list
		2) Currently stack and grammar is both in String so we need to convert into char
		3) Now we compare if the chars are equal
		4) if equal (pop stack, return grammar minus the reduced char as a String)
	*/
	public static void reduction() {

		/* Converts first element into a single string element */
		String var = String.valueOf(current_variable.charAt(0));
		String term = input.peek();

		/* Takes care of the "if" string */
		if (var.equals("i") && term.equals("i")) {
			for (int i=0; i<2; i++) {
				input.pop();
			}
			current_variable = current_variable.substring(2,current_variable.length());
			return;
		}

		if (var.equals("p") && term.equals("p")) {
			for (int i=0; i<5; i++) {
				input.pop();
			}
			current_variable = current_variable.substring(5,current_variable.length());
			return;
		}

		/* Base case, if both reached "$" then ACCEPTED */
		if ((var.equals("$")) && term.equals("$")) {
			current_state = "ACCEPTED";
			return;
		}

		/* If chars match on current_variable and input stack then reduce */
		if (input.peek().charAt(0)==current_variable.charAt(0)) {
			input.pop();
			current_variable = current_variable.substring(1,current_variable.length());
			return;
		}

		/* If chars result in a null spot in the parse tableor if
		   the stack ends on "$" before the variables do then it is REJECTED
		*/
		try {
			if (getString(var, term)==null) {
				current_state = "REJECTED";
				return;
			}
		} catch (ArrayIndexOutOfBoundsException e) {
			current_state = "REJECTED";
		}
	}



	/* Check if substitution if available */
	/* [EXPLANATION] (Assumes the variables and terminals are all valid)
		1) Reduce the stack and current variable before using this method
		2) Error case is first
		3) Take first element of the input stack and the current variable
		4) Update the stack and variables
	*/
	public static void substitution() {

		String var = String.valueOf(current_variable.charAt(0));
		String term = input.peek();
		String sub = getString(var, term);


		if (sub==null) {
			current_state = "REJECTED";
			return;
		}
		if (getString(var, term).equals("|")) {
		current_variable = current_variable.substring(1,current_variable.length());
			return;
		}

		current_variable = sub.substring(0, sub.length()) + current_variable.substring(1,current_variable.length());
	}





	/* Read from specified file and convert it into the stack */
	/* [Explanation] When read from the text file, it will be in strings so to put into stack, we
	    must convert the string to an individual string by converting into a char and back into a string
		1) Organise the textfile into a string
		2) Make conversion to stack
		3) Put string into stack
	*/
	public static void initialise(String args) {

		String fileName = args;
		String line = null;
		String fileContent = "";

		try {
			FileReader fileReader = new FileReader(fileName);

			BufferedReader bufferedReader = new BufferedReader(fileReader);

			while((line = bufferedReader.readLine()) != null) {
				fileContent += line.replace(" ", "");
			}

			bufferedReader.close();
		}
		catch(Exception e) {
			System.out.println("Unable to open file '" + fileName + "' or file does not exist.");
			System.exit(0);
		}


		input.push("$");
		String pusher = "";

		for (int i=fileContent.length()-1; i>=0; i--) {
			pusher = String.valueOf(fileContent.charAt(i));
			input.push(pusher);
		}
	}

	public static void printTrace() {
		System.out.print(input.toString() + "			");
		System.out.println(current_variable);
		return;
	}


	/* Error recovery message) */
	/* Parse all possible terminals related for var, input.peek() [top element] */
	public static void errorMessage(ArrayList<String> valid_terminals, String input) {

		String s = "{ ";
		System.out.printf("Error: got %s, but expected, ", input);

		for (int i=0; i<valid_terminals.size(); i++) {
				s += valid_terminals.get(i) + ", ";
		}

		s = s.substring(0, s.length()-2);
		System.out.printf("%s }.\n", s);
		return;
	}

	public static ArrayList<String> varTerminals(String var) {

		ArrayList<String> valid_terminals = new ArrayList<>();
		int y = findY(var);

		for (int i=0; i<16; i++) {
			try {
				if (!table[y][i].equals(null)) {
					valid_terminals.add(terminals[i]);
				} else if (table[y][i].equals("|")) {
					continue;
				}
			} catch (NullPointerException e) {
				continue;
			}
		}
		return valid_terminals;
	}

	public static void easterEgg (String args) {
		initialise(args);
		while (!input.empty()) {
			printTrace();
			reduction();
			if (current_state.equals("ACCEPTED")) { break; }
			if (current_state.equals("REJECTED")) {
				String var = String.valueOf(current_variable.charAt(0));
				errorMessage(varTerminals(var), input.peek());
				break;
			}
			substitution();
		}
		System.out.println(current_state);
		return;
	}

	public static void main(String[] args) {
		if (args.length < 1) {
			System.out.println("Command line argument(s) needed.");
			return;
		} else if (args.length == 1) {
			initialise(args[0]);
			while (!input.empty()) {
				printTrace();
				reduction();
				if (current_state.equals("ACCEPTED")) { break; }
				if (current_state.equals("REJECTED")) { break; }
				substitution();
			}
			System.out.println(current_state);
			return;
		} else if (args.length == 2) {
			if (args[1].equals("error")) {
				easterEgg(args[0]);
			} else {
				System.out.println("Second argument invalid. Try error.");
			}
			return;
		}

	}
}
