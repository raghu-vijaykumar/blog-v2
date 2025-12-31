---
slug: two-pointer-approach
title: 'DSA - Two Pointer Approach - (In Progress)'
tags: ["algorithms", "data-structures", "two-pointer", "DSA", "array"]
authors: [raghu-vijaykumar]
date: 2024-08-26
draft: true
---

![DSA - Two Pointer Approach - (In Progress)](./cover.png)

The two-pointer approach is a powerful technique used to solve problems involving arrays or strings. Here are the key characteristics and criteria to identify if a problem can be effectively solved using the two-pointer approach:

<!-- truncate -->

## Characteristics of Two-Pointer Problems

1. **Sorted Input**:

   - The input array is sorted or can be easily sorted. Sorting often simplifies the problem and makes the two-pointer technique applicable.

2. **Finding Pairs or Subarrays**:

   - The problem involves finding pairs, triplets, or subarrays that satisfy specific conditions, such as a target sum.

3. **Linear or Quadratic Time Complexity**:

   - The problem can be solved in linear \(O(n)\) or quadratic \(O(n^2)\) time complexity, which are typical of two-pointer solutions.

4. **Bidirectional Traversal**:
   - The solution requires traversing the array from both ends towards the center (or from the start towards the end using two pointers).

## Common Scenarios for Two-Pointer Approach

1. **Sum of Two Numbers**:

   - Problems where you need to find two numbers in an array that add up to a specific target.

   ```python
   def twoSum(nums, target):
       nums.sort()
       left, right = 0, len(nums) - 1
       while left < right:
           total = nums[left] + nums[right]
           if total == target:
               return [left, right]
           elif total < target:
               left += 1
           else:
               right -= 1
   ```

2. **Palindrome Checking**:

**Problem**: Check if a string is a palindrome by comparing characters from both ends towards the center.

```python
def isPalindrome(s):
    left, right = 0, len(s) - 1
    while left < right:
        if s[left] != s[right]:
            return False
        left += 1
        right -= 1
    return True
```

3. **Merging Sorted Arrays**
   **Problem**: Merge two sorted arrays into one sorted array.

```python
def merge(nums1, nums2):
    i, j = 0, 0
    result = []
    while i < len(nums1) and j < len(nums2):
        if nums1[i] < nums2[j]:
            result.append(nums1[i])
            i += 1
        else:
            result.append(nums2[j])
            j += 1
    result.extend(nums1[i:])
    result.extend(nums2[j:])
    return result
```

4. **Finding Subarrays with Specific Conditions**
   **Problem**: Find subarrays that satisfy certain conditions, such as a sum or product within a range.

```python
def subarraySum(nums, target):
    left, right, total = 0, 0, 0
    while right < len(nums):
        total += nums[right]
        right += 1
        while total > target:
            total -= nums[left]
            left += 1
        if total == target:
            return [left, right - 1]
```

## Steps to Identify and Apply the Two-Pointer Approach

### Check if Sorting Helps

1. **Determine if sorting the array can simplify the problem**:
   - If the array is already sorted or can be sorted with \(O(n \log n)\) complexity, the two-pointer approach might be applicable.

### Look for Pair or Subarray Problems

1. **Identify if the problem involves finding pairs, triplets, or subarrays with specific properties**:
   - Typical examples include finding pairs that sum to a target, triplets that sum to zero, or the longest subarray with a given condition.

### Consider the Traversal Directions

1. **Determine if moving pointers inward from both ends or outward from the start can help find the solution**:
   - Check if you can eliminate impossible solutions by moving pointers in a structured manner.

### Plan Pointer Movements

1. **Define the conditions for moving the left and right pointers**:
   - For instance, if the current sum is less than the target, move the left pointer right to increase the sum.
   - If the current sum is greater than the target, move the right pointer left to decrease the sum.

## Additional Considerations for Two-Pointer Approaches

### Edge Cases

Always consider edge cases such as empty arrays, arrays with only one element, or arrays where all elements are identical. These cases might affect how you initialize and move the pointers.

### Multiple Pointers

In some problems, especially those involving more complex conditions or more than two pointers (e.g., three pointers for **k**-Sum problems), adapting the basic two-pointer technique might be necessary.

#### Example: 3-Sum Problem

**Problem**: Given an array `nums` of `n` integers, find all unique triplets `(a, b, c)` in the array such that they add up to zero.

```python
def threeSum(nums):
    nums.sort()  # Step 1: Sort the array
    res = []

    for i in range(len(nums) - 2):  # Step 2: Iterate through the array
        if i > 0 and nums[i] == nums[i - 1]:  # Skip duplicate values
            continue

        left, right = i + 1, len(nums) - 1  # Step 3: Initialize two pointers

        while left < right:
            total = nums[i] + nums[left] + nums[right]  # Calculate the sum of the triplet

            if total == 0:
                res.append([nums[i], nums[left], nums[right]])  # Add the triplet to the result

                # Step 4: Move pointers and skip duplicates
                while left < right and nums[left] == nums[left + 1]:
                    left += 1
                while left < right and nums[right] == nums[right - 1]:
                    right -= 1

                left += 1
                right -= 1

            elif total < 0:
                left += 1  # Move the left pointer to the right to increase the sum
            else:
                right -= 1  # Move the right pointer to the left to decrease the sum

    return res
```

- **Sorting**: Sort the array `nums` first to facilitate easier triplet sum calculation and skip duplicates.

- **Iterating Through the Array**: Use a loop to iterate through the array `nums`, fixing one element (`nums[i]`) at a time as the potential first element of the triplet.

- **Initializing Pointers**: For each fixed element `nums[i]`, initialize two pointers (`left` and `right`) to find the remaining two elements (`nums[left]` and `nums[right]`).

- **Calculating Triplet Sum**: Calculate the sum `total = nums[i] + nums[left] + nums[right]`. Depending on whether `total` is equal to, less than, or greater than zero, adjust the pointers to find the next potential triplet.

- **Handling Duplicates**: Skip over duplicate values of `nums[i]`, `nums[left]`, and `nums[right]` to ensure each triplet in the result `res` is unique.

- **Moving Pointers**: Adjust `left` and `right` pointers based on the calculated `total` to ensure progress towards finding all possible triplets that sum to zero.

Given input array `nums = [-1, 0, 1, 2, -1, -4]`, the function `threeSum(nums)` will find all unique triplets `(a, b, c)` such that `a + b + c = 0`, resulting in: `[[-1, -1, 2], [-1, 0, 1]]`

### Array Modification

Be cautious when modifying the array during traversal, as it might affect subsequent pointer movements and the correctness of the solution. Make sure modifications are carefully handled.

#### Example: Remove Duplicates from Sorted Array

**Problem**: Given a sorted array `nums`, remove the duplicates in-place such that each element appears only once and return the new length.

```python
def removeDuplicates(nums):
    if not nums:
        return 0

    slow = 0

    for fast in range(1, len(nums)):
        if nums[fast] != nums[slow]:
            slow += 1
            nums[slow] = nums[fast]

    return slow + 1
```

- **Initialization**: Initialize two pointers, `slow` and `fast`, where `slow` points to the last unique element found and `fast` iterates through the array.
- **Iterate through the array**: Start iterating `fast` from the second element (`range(1, len(nums))`). Compare each element with the element at `slow`.
- **Modify array in-place**: If `nums[fast]` is different from `nums[slow]`, increment `slow` and update `nums[slow]` with `nums[fast]`. This modifies the array in-place to remove duplicates.
- **Return the new length**: After iterating through the array, `slow + 1` gives the length of the array with unique elements.

This example demonstrates modifying the array (`nums`) in-place while using two pointers (`slow` and `fast`) to efficiently remove duplicates.

### Time Complexity Analysis

While the two-pointer approach often provides efficient **O(n)** or **O(n log n)** solutions, depending on sorting requirements, verify that the chosen approach meets the problem's constraints and performance requirements.

### Scalability

Consider how scalable the solution is for larger inputs. While the two-pointer approach is efficient, ensure that it can handle maximum input sizes within acceptable time and space constraints.

#### Example: Finding Subarray with Maximum Sum

**Problem**: Given an array of integers, find the contiguous subarray (containing at least one number) which has the largest sum and return its sum.

```python
def maxSubArray(nums):
    if not nums:
        return 0

    max_sum = float('-inf')
    current_sum = 0

    for num in nums:
        current_sum += num
        if current_sum > max_sum:
            max_sum = current_sum
        if current_sum < 0:
            current_sum = 0

    return max_sum
```

- **Initialization**: Start with `max_sum` initialized to negative infinity (`float('-inf')`) to handle cases where all numbers are negative.
- **Iterate through the array**: Use a single pointer to traverse through the array and calculate the cumulative sum (`current_sum`).
- **Update maximum sum**: Update `max_sum` whenever `current_sum` exceeds it.
- **Reset sum**: Reset `current_sum` to zero if it becomes negative, ensuring the subarray starts fresh for potential maximum sums.

For the input array `nums = [-2, 1, -3, 4, -1, 2, 1, -5, 4]`, the maximum sum of a contiguous subarray is `6`, which corresponds to the subarray `[4, -1, 2, 1]`.

This example demonstrates a scalable two-pointer-like approach for finding the maximum sum of a contiguous subarray, ensuring efficiency even with larger inputs.
