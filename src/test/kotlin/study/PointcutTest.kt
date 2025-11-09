package study

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import org.springframework.aop.support.NameMatchMethodPointcut

class PointcutTest: FunSpec({
    context("Pointcut Test") {
        val pointcut = NameMatchMethodPointcut().apply {
            setMappedNames("hello*")
        }

        test("should always match class filter") {
            val studyServiceClass = StudyService::class.java
            pointcut.classFilter.matches(studyServiceClass) shouldBe true
            pointcut.classFilter.matches(String::class.java) shouldBe true
            pointcut.classFilter.matches(Any::class.java) shouldBe true
        }

        test("should match method names correctly") {
            val studyServiceClass = StudyService::class.java

            val helloMethod = studyServiceClass.getMethod("hello")
            val helloWorldMethod = studyServiceClass.getMethod("helloWorld")
            val hiMethod = studyServiceClass.getMethod("hi")
            val goodbyeMethod = studyServiceClass.getMethod("goodbye")

            pointcut.matches(helloMethod, studyServiceClass) shouldBe true
            pointcut.matches(helloWorldMethod, studyServiceClass) shouldBe true
            pointcut.matches(hiMethod, studyServiceClass) shouldBe false
            pointcut.matches(goodbyeMethod, studyServiceClass) shouldBe false
        }
    }
}) {
    companion object {
        private class StudyService {
            fun hello() {}
            fun helloWorld() {}
            fun hi() {}
            fun goodbye() {}
        }
    }
}