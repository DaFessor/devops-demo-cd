package com.via.doc1.devops_demo_cd.model;

import jakarta.persistence.*;
import lombok.Data; // Combines @Getter, @Setter, @ToString, @EqualsAndHashCode, @RequiredArgsConstructor
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity // Marks this class as a JPA entity (a table in the database)
@Table(name = "tasks") // Specifies the table name
@EntityListeners(AuditingEntityListener.class) // Enables JPA Auditing features (like @LastModifiedDate)
@Data // Lombok: Generates getters, setters, toString, equals, hashCode
@NoArgsConstructor // Lombok: Generates a no-argument constructor (required by JPA)
public class Task {

    @Id // Marks this field as the primary key
    @GeneratedValue(strategy = GenerationType.IDENTITY) // Configures auto-incrementing ID (suitable for PostgreSQL)
    private Long id;

    @Column(nullable = false, length = 100) // Database column constraints
    private String name;

    @Column(length = 1000) // Allows longer text for description
    private String description;

    @LastModifiedDate // Automatically set by Spring Data JPA Auditing on save/update
    @Column(name = "last_modified", nullable = false)
    private LocalDateTime lastModified;

    // Optional: Constructor for easier creation (without ID and lastModified)
    public Task(String name, String description) {
        this.name = name;
        this.description = description;
    }
}