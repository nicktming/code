public class Person {
	String name;
	int age;
	public Person() {}
	public Person(String name, int age) {
		this.name = name;
		this.age  = age;
	}
	
	@Override
	public String toString() {
		return "Person [name=" + name + ", age=" + age + "]";
	}
	@Override
	public boolean equals(Object obj) {

		if (this == obj) return true;
		if (obj instanceof Person) {
			Person p = (Person)obj;
			return p.name.equals(this.name);
		}
		return false;

		//return true;
	}
	
	@Override
	public int hashCode() {
		// TODO Auto-generated method stub
		return age;
	}
	
	
}
