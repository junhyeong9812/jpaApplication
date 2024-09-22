package jpabook.jpashop;


import com.fasterxml.jackson.datatype.hibernate6.Hibernate6Module;
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
	Hibernate6Module hibernate5Module() {
		return new Hibernate6Module();
		//Lazy로 하고 싶을 경우
//		Hibernate5Module hibernate5Module = new Hibernate5Module();
//		hibernate5Module().configure(Hibernate5Module.Feature.FORCE_LAZY_LOADING,true);
//		lazy모듈을 사용하는 거 자체가 그렇게 좋은 것이 아니다.
		//결과론적으로 엔티티를 직접 노출해야되기 때문이다.
	}
	//Hibernate6Module 5모듈은 javax기반이라 스프링 3.0버전에서는 사용불가

	//OSIV(OpenSession In View)와 성능 최적화
	//spring.jpa.open-in-view는 기본 허용이라는 warning이 나온다.
	//DB쿼리가 뷰가 렌더링될때까지 수행이 될 것이다.
	//open in view 옵션은 잘 사용해야 된다.
//	이 기본값을 애플리케이션 시작 시점에 warn을 남기는 이유가 존재하는데
//	기본적으로 jpa가 데이터베이스 커넥션을 처음 가지고 올 때는
//	영속성 컨텍스트와 큰 연관을 가지고 있는데
//	영속성 컨텍스트와 DB커넥션이 연결되어야 하는데
//	기본적으로 DB트렌잭션을 시작할 때 JPA의 영속성 컨텍스트가 DB커넥션을 가져온다
//	그렇게 얻는 커넥션은 OSIV가 있으면 트렌젝션이 끝나고 컨트롤러까지 반환을 안하고
//	가지고 있는다. 왜냐면 컨트롤러에서도 레이지 로딩을 통해 필요한 데이터를 가져와야할 경우가 있어서이다.
//	프록시 초기화를 위해서라도 영속성 컨텍스트가 DB커넥션을 가지고 살아있어야 한다.
//	API가 유저에게 반환될 때까지와 View라면 그 화면이 완전히 렌더링될 때까지
//	가지고 있는다.
//	open in view가 살아있었기 때문에 지연로딩이 가능했던 것이다.

//	하지만 어러한 전략은 트레이드 오프가 있는데 너무 오랫동안 DB커넥션을 가지고 있어서
//	실시간 트래픽에서는 커넥션이 말라버릴 수 있다.
//	컨트롤러에서 외부 API 호출하면 그 외부 API 대기시간만큼 추가적으로 커넥션 리소스를
//	반환하지 못하고 유지하는 것이다.
//
//	Open session in view를 끄면
//	트렌젝션 범위 내에서만 커넥션이 유지되는데
//	이러한 장점은 트렌젝션을 매우 타이트하게 사용할 수 있다.
//	그래서 매우 빠르게 커넥션을 유연하게 사용할 수 있다.
//	osiv가 켜저있어서 커넥션이 없다는 에러가 자주 발생할 수 있다.
//
//	하지만 OSIV를 끄면 모든 지연로딩을 트렌잭션 안에서 처리해야 된다.
//	따라서 지금까지 작성한 지연로딩 코드를 트렌잭션 내부에 작성해야 된다.
//	지연로딩을 하는 게 컨트롤러에서 하는 코드들이 있는데 이런 코드들을 모두
//			서비스 단으로 바꿔야 되는 것
	//만약 false로 바꾸고 오더 API에 대해서 요청을 해보면 에러가 난다.
	//could not initialize proxy에러가 나오면서
//	레이지 이니져라이제이션 에러가 나온다/
	//이걸 해결하기 위해서 패치조인이나 트렌잭션 내부로 컨트롤러의 코드를 옮겨서
	//트렌잭션 내부에서 동작하도록 해야된다.

	//이걸 해결하기 위한 방법은 많지만 대표적으로

}

