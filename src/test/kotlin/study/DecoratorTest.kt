package study

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk

// study.Handler 타입 정의
typealias Handler<A, B> = suspend (A) -> B

// study.Capability 정의
sealed interface Capability
object Read : Capability
object Write : Capability

class DecoratorTest : FunSpec({
    context("lazy loading: thunk & memoization") {
        test("Lazy Proxy") {
            class LazyProxy<T>(private val loader: suspend () -> T) {
                private var cached: T? = null
                suspend fun get(): T = cached ?: loader().also { cached = it }
            }

            // Mock loader 함수
            val loader: suspend () -> String = mockk()
            coEvery { loader() } returns "loaded data"

            val userProxy = LazyProxy(loader)

            // 첫 번째 호출
            val u1 = userProxy.get()
            u1 shouldBe "loaded data"

            // 두 번째 호출
            val u2 = userProxy.get()
            u2 shouldBe "loaded data"

            // 같은 인스턴스인지 확인
            u1 shouldBe u2

            // loader가 정확히 한 번만 호출되었는지 검증
            coVerify(exactly = 1) { loader() }
        }
    }

    context("Crosscutting Concerns") {
        test("Decorator Pattern") {
            // Request/Response 정의
            data class Request(val data: String)
            data class Response(val result: String)

            // Mock 함수들
            val doWork: suspend (Request) -> Response = mockk()
            val hasPermission: suspend () -> Boolean = mockk()

            coEvery { doWork(any()) } answers {
                Response("processed: ${firstArg<Request>().data}")
            }
            coEvery { hasPermission() } returns true

            // Decorators
            suspend fun <A, B> withLogging(h: Handler<A, B>, a: A): B {
                println("→ $a")
                val r = h(a)
                println("← $r")
                return r
            }

            suspend fun <A, B> withTxn(h: Handler<A, B>, a: A): B {
                // 트랜잭션 시뮬레이션
                return h(a)
            }

            suspend fun <A, B> withAuth(h: Handler<A, B>, a: A): B {
                require(hasPermission()) { "forbidden" }
                return h(a)
            }

            // 테스트 실행
            val service: Handler<Request, Response> = { req -> doWork(req) }

            val request = Request("test data")

            // Decorator 체인 실행: logging -> auth -> txn -> service
            fun <A, B> compose(vararg wraps: (Handler<A, B>) -> Handler<A, B>) =
                { base: Handler<A, B> -> wraps.foldRight(base) { w, acc -> w(acc) } }

            val decoratedService = compose<Request, Response>(
                { h -> { a -> withLogging(h, a) } },
                { h -> { a -> withAuth(h, a) } },
                { h -> { a -> withTxn(h, a) } }
            )(service)

            val response = decoratedService(request)

            // 검증
            response.result shouldBe "processed: test data"
            coVerify(exactly = 1) { hasPermission() }
            coVerify(exactly = 1) { doWork(request) }
        }
    }

    context("Access permission control") {
        test("study.Capability - study.Read/study.Write separation") {
            // 리포지토리 동작을 능력으로 분리
            class Repo<C : Capability>(private val store: MutableMap<Long, Long>) {
                fun balance(id: Long): Long = store[id] ?: 0L
            }

            // study.Write Capability를 가진 Repo만 deposit 가능
            fun Repo<Write>.deposit(id: Long, amount: Long) {
                (this as Repo<*>).let { repo ->
                    val store = repo::class.java.getDeclaredField("store").apply { isAccessible = true }
                        .get(repo) as MutableMap<Long, Long>
                    store[id] = balance(id) + amount
                }
            }

            // 팩토리: 읽기용/쓰기용 핸들 분리
            fun readOnlyRepo(store: MutableMap<Long, Long>) = Repo<Read>(store)
            fun readWriteRepo(store: MutableMap<Long, Long>) = Repo<Write>(store)

            // 사용
            val store = mutableMapOf(1L to 100L)
            val r = readOnlyRepo(store)
            val rw = readWriteRepo(store)

            // 읽기 전용 레포지토리로 조회
            r.balance(1L) shouldBe 100L

            // r.deposit(1L, 10)   // 컴파일 에러! (읽기 전용 보장)

            // 쓰기 가능한 레포지토리로 입금
            rw.deposit(1L, 50)
            rw.balance(1L) shouldBe 150L

            // 읽기 전용 레포지토리도 같은 store를 참조하므로 변경 확인 가능
            r.balance(1L) shouldBe 150L

            // 추가 입금
            rw.deposit(1L, 25)
            rw.balance(1L) shouldBe 175L

            // 존재하지 않는 계좌 조회
            r.balance(999L) shouldBe 0L

            // 존재하지 않는 계좌에 입금
            rw.deposit(999L, 100L)
            rw.balance(999L) shouldBe 100L
        }
    }
})