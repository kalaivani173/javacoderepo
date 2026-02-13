package com.npci.UPISim.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "psp_bank")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PspBank {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name; // Bank/PSP Name

    @Column(nullable = false, unique = true)
    private String orgId; // NPCI Org Id

    @Column(nullable = false, unique = true)
    private String iin; // Issuer Identification Number

    @Column(nullable = false, unique = true)
    private String ifsc; // IFSC Code

    @Column(nullable = false, unique = true)
    private String handle; // VPA Handle like @okaxis, @upi

    @Column(nullable = false)
    private String bankUrl; // Endpoint URL / Bank IP

    @Column(nullable = false)
    private String bankCode; // Internal Bank Code

    // 🆕 New fields for heartbeat tracking

    @Column(name = "last_heartbeat")
    private LocalDateTime lastHeartbeat;  // last time ReqHbt was received

    @Column(name = "status")
    private String status; // UP / DOWN / UNKNOWN
}
