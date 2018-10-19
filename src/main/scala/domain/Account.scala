package domain

final case class AccountId(value: Long) extends AnyVal
final case class Account(balance: BigDecimal, accountId: AccountId = AccountId(0L))
