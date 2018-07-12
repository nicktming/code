@DBTable(name="user")
public class User {
	@SQLString(30) String username;
	@SQLString(value=20,
			constraints=@Constraints(allowNull=false)) 
			String password;
	@SQLInteger int age;
	@SQLCharacter(value=15,
			constraints=@Constraints(primaryKey=true))
			String handle;
	@SQLBoolean(name="VIP") boolean isVIP;
}
