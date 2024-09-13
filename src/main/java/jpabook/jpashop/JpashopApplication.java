package jpabook.jpashop;

import com.fasterxml.jackson.datatype.hibernate5.Hibernate5Module;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class JpashopApplication {

	public static void main(String[] args) {
//		Hello hello = new Hello();
//		hello.setData("hello");
//		String data = hello.getData();
//		System.out.println("data = " + data);
		SpringApplication.run(JpashopApplication.class, args);
	}
	@Bean
	Hibernate5Module hibernate5Module() {
		return new Hibernate5Module();
		//Lazy로 하고 싶을 경우
//		Hibernate5Module hibernate5Module = new Hibernate5Module();
//		hibernate5Module().configure(Hibernate5Module.Feature.FORCE_LAZY_LOADING,true);
//		lazy모듈을 사용하는 거 자체가 그렇게 좋은 것이 아니다.
		//결과론적으로 엔티티를 직접 노출해야되기 때문이다.
	}

}
