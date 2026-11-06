package ch.unige.mssn.pmp.access.spring;

import ch.unige.mssn.pmp.access.model.AccessDecision;
import ch.unige.mssn.pmp.access.model.AccessRequest;
import ch.unige.mssn.pmp.access.model.Confidentiality;
import ch.unige.mssn.pmp.access.model.Subject;
import java.lang.reflect.Method;
import java.time.Clock;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.context.expression.BeanFactoryResolver;
import org.springframework.context.expression.MethodBasedEvaluationContext;
import org.springframework.core.DefaultParameterNameDiscoverer;
import org.springframework.core.ParameterNameDiscoverer;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;

/** Point d'application : la méthode protégée ne s'exécute qu'après une décision favorable, écrite au journal. */
@Aspect
public class AccessControlAspect {

    private static final Logger LOG = LoggerFactory.getLogger(AccessControlAspect.class);
    private static final ExpressionParser PARSER = new SpelExpressionParser();
    private static final ParameterNameDiscoverer NAMES = new DefaultParameterNameDiscoverer();

    private final AccessGuard guard;
    private final SubjectResolver subjects;
    private final BeanFactory beans;
    private final Clock clock;

    public AccessControlAspect(AccessGuard guard, SubjectResolver subjects, BeanFactory beans, Clock clock) {
        this.guard = guard;
        this.subjects = subjects;
        this.beans = beans;
        this.clock = clock;
    }

    @Around("@annotation(controlled)")
    public Object enforce(ProceedingJoinPoint call, AccessControlled controlled) throws Throwable {
        AccessDecision decision = guard.check(request(call, controlled));
        AccessContext.set(decision);
        try {
            return call.proceed();
        } finally {
            AccessContext.clear();
        }
    }

    private AccessRequest request(ProceedingJoinPoint call, AccessControlled controlled) {
        Subject subject = subjects.current();
        Method method = ((MethodSignature) call.getSignature()).getMethod();
        var context = new MethodBasedEvaluationContext(call.getTarget(), method, call.getArgs(), NAMES);
        context.setBeanResolver(new BeanFactoryResolver(beans));
        String patient = null;
        try {
            patient = evaluate(controlled.patient(), context, String.class);
            return new AccessRequest(subject, patient, controlled.resource(), controlled.action(),
                    evaluate(controlled.dataLevel(), context, Confidentiality.class),
                    evaluate(controlled.emergencyJustification(), context, String.class),
                    evaluate(controlled.reference(), context, String.class),
                    clock.instant());
        } catch (RuntimeException e) {
            LOG.error("Demande impossible à formuler pour {}/{}", controlled.resource(), controlled.action(), e);
            throw guard.refuseUndecidable(new AccessRequest(subject, patient, controlled.resource(), controlled.action(),
                    null, null, null, clock.instant()));
        }
    }

    private static <T> T evaluate(String expression, MethodBasedEvaluationContext context, Class<T> type) {
        return expression.isEmpty() ? null : PARSER.parseExpression(expression).getValue(context, type);
    }
}
