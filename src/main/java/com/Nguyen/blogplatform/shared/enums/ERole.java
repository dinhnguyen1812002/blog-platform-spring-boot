package com.Nguyen.blogplatform.shared.enums;



/**
 * Two-tier model (stored with the ROLE_ prefix, checked with hasRole('X')):
 *  - ROLE_USER:  every account; reads, interacts, and writes/manages its OWN content
 *  - ROLE_ADMIN: manages ALL content and the whole platform (users, reports, newsletter…)
 */
public enum ERole {
    ROLE_USER,
    ROLE_ADMIN
}
