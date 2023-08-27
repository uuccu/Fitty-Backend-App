package fittybackendapp.domain.diet.entity

import fittybackendapp.common.entitiybase.AuditLoggingBase
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import jakarta.validation.constraints.NotNull
import org.hibernate.annotations.OnDelete
import org.hibernate.annotations.OnDeleteAction

@Entity
@Table(name = "diet_diet_record")
class DietDietRecord(
    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "diet_record_id", nullable = false)
    var dietRecord: DietRecord,

    @NotNull
    @Column(name = "weight", nullable = false)
    var weight: Long,

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "diet_id", nullable = false)
    var diet: Diet
): AuditLoggingBase() {
    @Id
    @Column(name = "id", nullable = false)
    var id: Long? = null
}
