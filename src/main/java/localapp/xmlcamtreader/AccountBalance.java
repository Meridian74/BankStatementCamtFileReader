package localapp.xmlcamtreader;

import java.util.HashMap;
import java.util.Map;

public class AccountBalance {
	
	// egyenlegek típusonként (záró, nyitó, zárolás)
	private Map<AccountBalanceType, Double> balances = new HashMap<>();
	
	// a statement indexe, amelyikből az egyenlegek származnak
	private int statementIndex;
	

	public void addBalance(AccountBalanceType type, Double amount) {
		balances.put(type, amount);
	}
	
	public Double getBalanceByType(AccountBalanceType type) {
		return balances.get(type);
	}
	
	public void setStatementIndex(int statementIndex) {
		this.statementIndex = statementIndex;
	}
	
	public int getStatementIndex() {
		return statementIndex;
	}
}