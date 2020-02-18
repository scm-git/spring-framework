package com.test;

/**
 * =========================1. 成员内部类：=========================
 * 成员内部类就像一个实例变量，他依赖于外部类的实例存在 --> 必须先有外部类的实例  才能创建成员内部类对象
 * 成员内部类可以访问它的外部类的所有成员变量和方法，不管是静态的还是非静态的都可以。
 * 内部类对象中不能有静态成员
 */
public class Outer {
	private int i = 10;

	class Inner{
		// 成员内部类对象中不能有静态成员
		// public static Object obj = new Object();

		public void seeOuter(){
			System.out.print(i);
		}
	}

	/**
	 * 外部类实例方法中可以直接创建内部内
	 */
	public void makeInner(){
		Inner inner = this.new Inner();
		Inner in = new Inner();
		in.seeOuter();
	}

	/**
	 * 必须先有外部类的实例  才能创建成员内部类对象
	 * 外部类静态方法中必须先创建外部类实例变量，再用这个外部类创建的实例变量来创建内部类
	 */
	public static void makeInnerInStatic(){
		Outer outer = new Outer();
		Outer.Inner in = outer.new Inner();
		in.seeOuter();
	}
}

/**
 * =========================2. 局部内部类local inner class=========================
 * 也叫做方法内部类，就是把放在方法中，最少用的一种
 *
 * 像局部变量一样，不能被public, protected, private和static修饰。
 * 局部内部类在方法中定义，所以只能在方法中使用，即只能在方法当中生成局部内部类的实例并且调用其方法。
 *
 * 局部内部类只能在声明的方法内是可见的，因此定义局部内部类之后，想用的话就要在方法内直接实例化，
 * 记住这里顺序不能反了，一定是要先声明后使用，否则编译器会说找不到。
 *
 * 方法内部类的修饰符：
 *   与成员内部类不同，方法内部类更像一个局部变量。
 *   可以用于修饰方法内部类的只有final和abstract。
 *
 * 注意事项：
 * 1. 方法内部类只能在定义该内部类的方法内实例化，不可以在此方法外对其实例化
 * 2. 方法内部类对象不能使用该内部类所在方法的非final局部变量。
 * 原因：
 *     因为方法的局部变量位于栈上，只存在于该方法的生命期内。当一个方法结束，其栈结构被删除，局部变量成为历史。
 *     但是该方法结束之后，在方法内创建的内部类对象可能仍然存在于堆中！例如，如果对它的引用被传递到其他某些代码，并存储在一个成员变量内。
 *     正因为不能保证局部变量的存活期和方法内部类对象的一样长，所以内部类对象不能使用它们。下面是完整的例子：
 * 3. 静态方法内的方法内部类
 *    静态方法是没有this引用的，因此在静态方法内的内部类遭受同样的待遇，即：只能访问外部类的静态成员。
 */
class LocalInnerClass {
	public void doSomething() {
		class Inner{
			public void seeOuter(){
				System.out.println("inner class");
			}
		}

		Inner inner = new Inner();
		inner.seeOuter();
	}
}

/**
 * =========================3. 匿名内部类Anonymous Inner Class=========================
 * 顾名思义，没有名字的内部类，最常用的一种内部类，通常是作为一个方法参数
 * 匿名内部类就是没有名字的局部内部类，不使用关键字class, extends, implements, 没有构造方法。
 * 匿名内部类隐式地继承了一个父类或者实现了一个接口。
 *
 * 1. 继承式的匿名内部类。
 * 2. 接口式的匿名内部类。
 * 3. 参数式的匿名内部类。
 *
 * 建立匿名内部类的关键点是重写父类的一个或多个方法。再强调一下，是重写父类的方法，而不是创建新的方法。
 * 因为用父类的引用不可能调用父类本身没有的方法！创建新的方法是多余的。简言之，参考多态。
 */
class Car {
	public void drive(){
		System.out.println("Driving a car!");
	}
}

interface  Vehicle {
	public void drive();
}

class Bar{
	void doStuff(Foo f){}
}

interface Foo {
	void foo();
}

class Test{
	public static void main(String[] args) {
		// 1. 示例1： 继承式的匿名内部类
		Car car = new Car(){
			public void drive(){
				System.out.println("Driving another car!");
			}
		};
		car.drive();

		// 2. 示例2： 接口式的匿名内部类
		Vehicle v = new Vehicle(){
			public void drive(){
				System.out.println("Driving a car!");
			}
		};
		v.drive();

		// 3. 实例3： 参数式的匿名内部类
		Bar b = new Bar();
		b.doStuff(new Foo(){
			public void foo(){
				System.out.println("foofy");
			}
		});
	}
}

/**
 * =========================4. 静态内部类static inner class=========================
 * 在定义成员内部类的时候，可以在其前面加上一个权限修饰符static。此时这个内部类就变为了静态内部类。
 * 只可以访问外部类的静态成员和静态方法，包括了私有的静态成员和方法。
 * 同样会被编译成一个完全独立的.class文件，名称为OuterClass$InnerClass.class的形式。
 * 生成静态内部类对象的方式为：OuterClass.InnerClass inner = new OuterClass.InnerClass();
 * 
 * 静态内部类的使用目的与限制： https://www.cnblogs.com/wihainan/p/4773076.html
 */
class StaticInnerClass {
	private static int a = 1;
	// 静态内部类
	public static class Inner
	{
		public void test()
		{
			// 静态内部类可以访问外部类的静态成员
			// 并且它只能访问静态的
			System.out.println(a);
		}

	}
}
