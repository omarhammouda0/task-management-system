# 🔧 TEST FIX GUIDE

## The Problem
The tests are failing because they reference a mocked `SecurityContext` that doesn't exist anymore.

## The Solution

### Step 1: Find and Replace Pattern

**Search for:**
```java
Authentication auth = new UsernamePasswordAuthenticationToken(
    adminUser.getEmail(), 
    null, 
    List.of(new SimpleGrantedAuthority("ROLE_ADMIN"))
);

when(securityContext.getAuthentication()).thenReturn(auth);
when(userRepository.findByEmailIgnoreCase(adminUser.getEmail()))
        .thenReturn(Optional.of(adminUser));
```

**Replace with:**
```java
setupAuthentication(adminUser);
when(userRepository.findByEmailIgnoreCase(adminUser.getEmail()))
        .thenReturn(Optional.of(adminUser));
```

### Step 2: For Anonymous Authentication Tests

**Search for:**
```java
when(securityContext.getAuthentication()).thenReturn(null);
```

**Replace with:**
```java
SecurityContextHolder.clearContext();
```

### Step 3: Apply to All Tests

Do the same replacement for:
- `memberUser`
- `managerUser`
- `targetUser`

## Quick Fix Commands

In your IDE:
1. Press Ctrl+H (Find and Replace)
2. Use these patterns one by one

### Pattern 1: Admin User Setup
**Find:** `Authentication auth = new UsernamePasswordAuthenticationToken\(\s*adminUser\.getEmail\(\),\s*null.*?\);\s*when\(securityContext\.getAuthentication\(\)\)\.thenReturn\(auth\);`
**Replace:** `setupAuthentication(adminUser);`

### Pattern 2: Member User Setup
**Find:** `Authentication auth = new UsernamePasswordAuthenticationToken\(\s*memberUser\.getEmail\(\),\s*null.*?\);\s*when\(securityContext\.getAuthentication\(\)\)\.thenReturn\(auth\);`
**Replace:** `setupAuthentication(memberUser);`

### Pattern 3: Null Authentication
**Find:** `when\(securityContext\.getAuthentication\(\)\)\.thenReturn\(null\);`
**Replace:** `SecurityContextHolder.clearContext();`


