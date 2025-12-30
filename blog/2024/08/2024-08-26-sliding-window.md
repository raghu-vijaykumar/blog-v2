---
slug: sliding-window
title: 'DSA - Sliding Window - (In Progress)'
tags: ["algorithms", "data-structures", "sliding-window", "DSA", "array"]
authors: [raghu-vijaykumar]
date: 2024-08-26
---

![DSA - Sliding Window - (In Progress)](./assets/cover.png)

The sliding window technique is a powerful approach used to solve a variety of problems, especially those involving subarrays or substrings. Here are the key characteristics and criteria to identify if a problem can be effectively solved using the sliding window technique:

<!-- truncate -->

## Characteristics of Sliding Window Problems

- **Contiguous Subarrays or Subsequences**:

  - The problem involves finding a subarray or substring with certain properties, such as a fixed length or a sum within a range.

- **Optimal Subarray/Subsequence**:

  - The problem requires finding an optimal subarray/subsequence, such as the longest, shortest, or one with the maximum/minimum sum/product.

- **Fixed or Variable Window Size**:
  - The sliding window can be either fixed in size (e.g., finding all subarrays of length k) or variable (e.g., finding the smallest subarray with a sum greater than a target).

## Common Scenarios for Sliding Window Technique

- **Maximum/Minimum Sum Subarray of Fixed Length**:

  - Problems where you need to find the maximum or minimum sum of a subarray of a fixed length.

    ```python
    def maxSumSubarray(nums, k):
        max_sum = current_sum = sum(nums[:k])
        for i in range(k, len(nums)):
            current_sum += nums[i] - nums[i - k]
            max_sum = max(max_sum, current_sum)
        return max_sum
    ```

- **Longest Substring with K Distinct Characters**:

  - Problems where you need to find the longest substring containing at most k distinct characters.

    ```python
    def lengthOfLongestSubstringKDistinct(s, k):
        char_map = {}
        left = 0
        max_length = 0
        for right in range(len(s)):
            char_map[s[right]] = char_map.get(s[right], 0) + 1
            while len(char_map) > k:
                char_map[s[left]] -= 1
                if char_map[s[left]] == 0:
                    del char_map[s[left]]
                left += 1
            max_length = max(max_length, right - left + 1)
        return max_length
    ```

- **Smallest Subarray with Sum Greater than a Given Value**:

  - Problems where you need to find the smallest subarray with a sum greater than a given target.

    ```python
    def minSubArrayLen(target, nums):
        left = 0
        current_sum = 0
        min_length = float('inf')
        for right in range(len(nums)):
            current_sum += nums[right]
            while current_sum >= target:
                min_length = min(min_length, right - left + 1)
                current_sum -= nums[left]
                left += 1
        return min_length if min_length != float('inf') else 0
    ```

## Steps to Identify and Apply the Sliding Window Technique

1. **Determine if the Problem Involves Subarrays or Subsequences**:

   - Check if the problem requires examining or optimizing properties of contiguous subarrays or subsequences.

2. **Identify the Window Size**:

   - Determine if the window size is fixed or variable.
   - For fixed-size windows, the problem often involves a specific length k.
   - For variable-size windows, the problem usually has a condition to expand or contract the window.

3. **Use a Two-Pointer Approach**:

   - Typically, the sliding window technique uses two pointers, left and right, to represent the current window.
   - Adjust the pointers to expand or contract the window based on the problem's requirements.

4. **Maintain a Running Calculation**:
   - Keep track of the current state of the window (e.g., sum, product, or character count) as you slide the window across the array or string.
   - Update the running calculation efficiently to reflect changes in the window.

## Example Problem: Maximum Sum Subarray of Size K

**Problem Statement**: Given an array of integers and a number k, find the maximum sum of a subarray of size k.

**Solution Using Sliding Window Technique**:

```python
def maxSumSubarray(nums, k):
    max_sum = current_sum = sum(nums[:k])  # Step 1: Initialize window and calculate initial sum
    for i in range(k, len(nums)):  # Step 2: Slide the window across the array
        current_sum += nums[i] - nums[i - k]  # Step 3: Update the window sum
        max_sum = max(max_sum, current_sum)  # Step 4: Track the maximum sum found
    return max_sum

# Sample Input
nums = [1, 2, 3, 4, 5, 6, 7, 8, 9, 10]
k = 3

# Expected Output: 27 (subarray [8, 9, 10])
print(maxSumSubarray(nums, k))
```

By analyzing the problem's characteristics and determining if the sliding window technique is applicable, you can effectively use this technique to solve a wide range of problems efficiently.
