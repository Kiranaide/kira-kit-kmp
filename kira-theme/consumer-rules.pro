# Consumer ProGuard/R8 rules for :kira-theme
# Allow full R8 shrinking, inlining, and class merging while keeping Compose runtime stability metadata.
-dontwarn com.kirakit.theme.**
