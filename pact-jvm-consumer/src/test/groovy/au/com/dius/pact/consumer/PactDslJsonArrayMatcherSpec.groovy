package au.com.dius.pact.consumer

import au.com.dius.pact.consumer.dsl.PactDslJsonArray
import au.com.dius.pact.consumer.dsl.PactDslJsonRootValue
import au.com.dius.pact.model.PactSpecVersion
import au.com.dius.pact.model.matchingrules.DateMatcher
import au.com.dius.pact.model.matchingrules.MaxTypeMatcher
import au.com.dius.pact.model.matchingrules.MinTypeMatcher
import au.com.dius.pact.model.matchingrules.NumberTypeMatcher
import au.com.dius.pact.model.matchingrules.TypeMatcher
import groovy.json.JsonSlurper
import spock.lang.Specification

class PactDslJsonArrayMatcherSpec extends Specification {

    private PactDslJsonArray subject

    def setup() {
      subject = new PactDslJsonArray()
    }

    def 'String Matcher Throws Exception If The Example Does Not Match The Pattern'() {
      when:
      subject.stringMatcher('[a-z]+', 'dfhdsjf87fdjh')

      then:
      thrown(InvalidMatcherException)
    }

    def 'Hex Matcher Throws Exception If The Example Is Not A Hexadecimal Value'() {
      when:
      subject.hexValue('dfhdsjf87fdjh')

      then:
      thrown(InvalidMatcherException)
    }

    def 'UUID Matcher Throws Exception If The Example Is Not A UUID'() {
      when:
      subject.uuid('dfhdsjf87fdjh')

      then:
      thrown(InvalidMatcherException)
    }

    def 'Allows Like Matchers When The Array Is The Root'() {
      given:
      Date date = new Date()
      subject = (PactDslJsonArray) PactDslJsonArray.arrayEachLike()
          .date('clearedDate', 'mm/dd/yyyy', date)
          .stringType('status', 'STATUS')
          .decimalType('amount', 100.0)
          .closeObject()

      expect:
        new JsonSlurper().parseText(subject.body.toString()) == [
          [amount: 100, clearedDate: date.format('mm/dd/yyyy'), status: 'STATUS']
        ]
        subject.matchers.matchingRules == [
          '': [new MinTypeMatcher(0)],
          '[*].amount': [new NumberTypeMatcher(NumberTypeMatcher.NumberType.DECIMAL)],
          '[*].clearedDate': [new DateMatcher('mm/dd/yyyy')],
          '[*].status': [new TypeMatcher()]
        ]
    }

    def 'Allows Like Min Matchers When The Array Is The Root'() {
      given:
      Date date = new Date()
      subject = (PactDslJsonArray) PactDslJsonArray.arrayMinLike(1)
          .date('clearedDate', 'mm/dd/yyyy', date)
          .stringType('status', 'STATUS')
          .decimalType('amount', 100.0)
          .closeObject()

      expect:
        new JsonSlurper().parseText(subject.body.toString()) == [
          [amount: 100, clearedDate: date.format('mm/dd/yyyy'), status: 'STATUS']
        ]
        subject.matchers.matchingRules == [
          '': [new MinTypeMatcher(1)],
          '[*].amount': [new NumberTypeMatcher(NumberTypeMatcher.NumberType.DECIMAL)],
          '[*].clearedDate': [new DateMatcher('mm/dd/yyyy')],
          '[*].status': [new TypeMatcher()]
        ]
    }

    def 'Allows Like Max Matchers When The Array Is The Root'() {
      given:
      Date date = new Date()
      subject = (PactDslJsonArray) PactDslJsonArray.arrayMaxLike(10)
          .date('clearedDate', 'mm/dd/yyyy', date)
          .stringType('status', 'STATUS')
          .decimalType('amount', 100.0)
          .closeObject()

      expect:
        new JsonSlurper().parseText(subject.body.toString()) == [
          [amount: 100, clearedDate: date.format('mm/dd/yyyy'), status: 'STATUS']
        ]
        subject.matchers.matchingRules == [
          '': [new MaxTypeMatcher(10)],
          '[*].amount': [new NumberTypeMatcher(NumberTypeMatcher.NumberType.DECIMAL)],
          '[*].clearedDate': [new DateMatcher('mm/dd/yyyy')],
          '[*].status': [new TypeMatcher()]
        ]
    }

  def 'root array each like allows the number of examples to be set'() {
    given:
    subject = PactDslJsonArray.arrayEachLike(3)
      .date('defDate')
      .decimalType('cost')
      .closeObject()

    when:
    def result = new JsonSlurper().parseText(subject.body.toString())

    then:
    result.size == 3
    result.every { it.keySet() == ['defDate', 'cost'] as Set }
  }

  def 'root array min like allows the number of examples to be set'() {
    given:
    subject = PactDslJsonArray.arrayMinLike(2, 3)
      .date('defDate')
      .decimalType('cost')
      .closeObject()

    when:
    def result = new JsonSlurper().parseText(subject.body.toString())

    then:
    result.size == 3
    result.every { it.keySet() == ['defDate', 'cost'] as Set }
  }

  def 'root array max like allows the number of examples to be set'() {
    given:
    subject = PactDslJsonArray.arrayMaxLike(10, 3)
      .date('defDate')
      .decimalType('cost')
      .closeObject()

    when:
    def result = new JsonSlurper().parseText(subject.body.toString())

    then:
    result.size == 3
    result.every { it.keySet() == ['defDate', 'cost'] as Set }
  }

  def 'each like allows the number of examples to be set'() {
    given:
    subject = new PactDslJsonArray()
      .eachLike(2)
        .date('defDate')
        .decimalType('cost')
        .closeObject()
      .closeArray()

    when:
    def result = new JsonSlurper().parseText(subject.body.toString())

    then:
    result.first().size == 2
    result.first().every { it.keySet() == ['defDate', 'cost'] as Set }
  }

  def 'min like allows the number of examples to be set'() {
    given:
    subject = new PactDslJsonArray()
      .minArrayLike(1, 2)
        .date('defDate')
        .decimalType('cost')
        .closeObject()
      .closeArray()

    when:
    def result = new JsonSlurper().parseText(subject.body.toString())

    then:
    result.first().size == 2
    result.first().every { it.keySet() == ['defDate', 'cost'] as Set }
  }

  def 'max like allows the number of examples to be set'() {
    given:
    subject = new PactDslJsonArray()
      .maxArrayLike(10, 2)
        .date('defDate')
        .decimalType('cost')
        .closeObject()
      .closeArray()

    when:
    def result = new JsonSlurper().parseText(subject.body.toString())

    then:
    result.first().size == 2
    result.first().every { it.keySet() == ['defDate', 'cost'] as Set }
  }

  def 'eachlike supports matching arrays of basic values'() {
    given:
    subject = new PactDslJsonArray()
      .eachLike(PactDslJsonRootValue.stringType('eachLike'))
      .maxArrayLike(2, PactDslJsonRootValue.stringType('maxArrayLike'))
      .minArrayLike(2, PactDslJsonRootValue.stringType('minArrayLike'))

    when:
    def result = subject.body.toString()

    then:
    result == '[["eachLike"],["maxArrayLike"],["minArrayLike","minArrayLike"]]'
    subject.matchers.toMap(PactSpecVersion.V2) == [
      '$.body[1]': [max: 2, match: 'type'],
      '$.body[2]': [min: 2, match: 'type'],
      '$.body[0]': [min: 0, match: 'type'],
      '$.body[1][*]': [match: 'type'],
      '$.body[2][*]': [match: 'type'],
      '$.body[0][*]': [match: 'type']
    ]
  }
}
