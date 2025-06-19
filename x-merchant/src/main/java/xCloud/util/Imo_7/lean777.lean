import Mathlib

-- Theorem for IMO 1974 Shortlist Problem 7
theorem imo_1974_shl_7 {k : ℕ} (hk : k ≠ 0) (a b : Fin k → ℕ)
  (hpos : ∀ i, b i ≠ 0) -- b_i are positive integers
  (hcoprime : ∀ i, Nat.Coprime (a i) (b i)) : -- a_i, b_i are coprime
  let m := (Finset.univ : Finset (Fin k)).lcm b; -- m is the lcm of b_i
  (Finset.univ : Finset (Fin k)).gcd (fun i => a i * m / b i) = Finset.univ.gcd a := by
  -- Define m as the lcm of b_i
  set m := (Finset.univ : Finset (Fin k)).lcm b
  -- Define the two gcds
  let left_gcd := (Finset.univ : Finset (Fin k)).gcd (fun i => a i * m / b i)
  let right_gcd := Finset.univ.gcd a
  -- Prove the gcds are equal by showing they divide each other
  apply Nat.dvd_antisymm
  -- Part 1: right_gcd divides left_gcd
  { apply Finset.gcd_dvd
    intro i hi
    -- right_gcd divides each a_i
    have h_dvd_ai : right_gcd ∣ a i := Finset.dvd_gcd hi
    -- Rewrite a_i * m / b_i
    have h_div : b i ∣ m := Finset.dvd_lcm hi
    have h_term : a i * m / b i = a i * (m / b i) := by
      rw [← Nat.mul_div_assoc a h_div]
    rw [h_term]
    -- right_gcd divides a_i * (m / b_i)
    exact dvd_mul_of_dvd_left h_dvd_ai _ }
  -- Part 2: left_gcd divides right_gcd
  { apply Finset.gcd_dvd
    intro i hi
    -- left_gcd divides each a_i * m / b_i
    have h_dvd_term : left_gcd ∣ a i * m / b i := Finset.dvd_gcd hi
    -- Since gcd(a_i, b_i) = 1, prove left_gcd divides a_i
    have h_div : b i ∣ m := Finset.dvd_lcm hi
    have h_coprime : Nat.Coprime (a i) (b i) := hcoprime i
    -- Rewrite a_i * m / b_i
    have h_term : a i * m / b i = a i * (m / b i) := by
      rw [← Nat.mul_div_assoc a h_div]
    rw [h_term] at h_dvd_term
    -- left_gcd divides a_i * (m / b_i), need to show it divides a_i
    have h_coprime_m_div_bi : Nat.Coprime (a i) (m / b i) := by
      apply Nat.coprime_of_dvd' h_coprime
      intro p hp h_dvd_m_div_bi h_dvd_bi
      have h_dvd_m : p ∣ m := by
        rw [Nat.div_mul_cancel h_div] at h_dvd_m_div_bi
        exact h_dvd_m_div_bi
      have h_dvd_some_bj : ∃ j, p ∣ b j := by
        exact Nat.dvd_lcm_iff.mp h_dvd_m hi
      obtain ⟨j, h_dvd_bj⟩ : ∃ j, p ∣ b j := h_dvd_some_bj
      have h_not_dvd_ai : ¬ p ∣ a i := by
        intro h_dvd_ai
        have h_gcd_ai_bi : p ∣ Nat.gcd (a i) (b i) := Nat.dvd_gcd h_dvd_ai h_dvd_bi
        rw [h_coprime] at h_gcd_ai_bi
        exact hp (Nat.dvd_one.mp h_gcd_ai_bi)
      exact h_not_dvd_ai h_dvd_m_div_bi
    exact Nat.coprime.dvd_of_dvd_mul_right h_coprime_m_div_bi h_dvd_term }