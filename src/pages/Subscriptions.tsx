import { useEffect, useState } from "react";
import { apiFetch } from "../api";
import { getApiErrorMessage, getNetworkErrorMessage } from "../errorHandling";
import "../css/Subscriptions.css";

type Plan = "REGULAR" | "PREMIUM";

type SubscriptionInfo = {
    plan: Plan;
    cancellationScheduled: boolean;
    subscriptionEnd: string | null;
};

function formatSubscriptionEnd(dateString: string | null): string {
    if (!dateString) return "";

    const date = new Date(dateString);

    if (Number.isNaN(date.getTime())) {
        return dateString;
    }

    return date.toLocaleDateString("en-GB");
}

function Subscriptions() {
    const [loadingPremium, setLoadingPremium] = useState(false);
    const [cancelLoading, setCancelLoading] = useState(false);
    const [resumeLoading, setResumeLoading] = useState(false);
    const [loadingSubscription, setLoadingSubscription] = useState(true);
    const [subscriptionInfo, setSubscriptionInfo] = useState<SubscriptionInfo | null>(null);
    const [message, setMessage] = useState("");
    const [messageType, setMessageType] = useState<"success" | "error" | "">("");

    useEffect(() => {
        async function loadSubscriptionInfo() {
            try {
                setLoadingSubscription(true);

                const res = await apiFetch("/api/v1/users/my-plan", {
                    method: "GET",
                });

                if (!res.ok) {
                    setMessage(await getApiErrorMessage(res, "Failed to load subscription details."));
                    setMessageType("error");
                    return;
                }

                const data: SubscriptionInfo = await res.json();
                setSubscriptionInfo(data);

                if (data.cancellationScheduled && data.subscriptionEnd) {
                    setMessage(
                        `Your subscription has been cancelled and will expire on ${formatSubscriptionEnd(data.subscriptionEnd)}.`
                    );
                    setMessageType("success");
                }
            } catch (e) {
                console.error("Load subscription info failed:", e);
                setMessage(getNetworkErrorMessage());
                setMessageType("error");
            } finally {
                setLoadingSubscription(false);
            }
        }

        loadSubscriptionInfo();
    }, []);

    async function handleBuyPremium() {
        try {
            setLoadingPremium(true);
            setMessage("");
            setMessageType("");

            const res = await apiFetch("/api/v1/stripe/checkout", {
                method: "POST",
                headers: { "Content-Type": "application/json" }
            });

            if (!res.ok) {
                setMessage(await getApiErrorMessage(res, "Failed to create checkout session."));
                setMessageType("error");
                return;
            }

            const data = await res.json();

            if (data.checkoutUrl) {
                window.location.href = data.checkoutUrl;
                return;
            }

            setMessage("Checkout URL was not returned by the server.");
            setMessageType("error");
        } catch (e) {
            console.error("Buy premium failed:", e);
            setMessage(getNetworkErrorMessage());
            setMessageType("error");
        } finally {
            setLoadingPremium(false);
        }
    }

    async function handleCancelSubscription() {
        try {
            setCancelLoading(true);
            setMessage("");
            setMessageType("");

            const res = await apiFetch("/api/v1/subscriptions/cancel", {
                method: "PATCH",
                headers: { "Content-Type": "application/json" }
            });

            if (!res.ok) {
                setMessage(await getApiErrorMessage(res, "Failed to cancel subscription."));
                setMessageType("error");
                return;
            }

            const data = await res.json();

            setMessage(data.message || "Subscription scheduled for cancellation.");
            setMessageType("success");

            setSubscriptionInfo((prev) => {
                if (!prev) return prev;

                return {
                    ...prev,
                    cancellationScheduled: true
                };
            });
        } catch (e) {
            console.error("Cancel subscription failed:", e);
            setMessage(getNetworkErrorMessage());
            setMessageType("error");
        } finally {
            setCancelLoading(false);
        }
    }

    async function handleResumeSubscription() {
        try {
            setResumeLoading(true);
            setMessage("");
            setMessageType("");

            const res = await apiFetch("/api/v1/subscriptions/resume", {
                method: "PATCH",
                headers: { "Content-Type": "application/json" }
            });

            if (!res.ok) {
                setMessage(await getApiErrorMessage(res, "Failed to resume subscription."));
                setMessageType("error");
                return;
            }

            const data = await res.json();

            setMessage(data.message || "Subscription resumed successfully.");
            setMessageType("success");

            setSubscriptionInfo((prev) => {
                if (!prev) return prev;

                return {
                    ...prev,
                    cancellationScheduled: false
                };
            });
        } catch (e) {
            console.error("Resume subscription failed:", e);
            setMessage(getNetworkErrorMessage());
            setMessageType("error");
        } finally {
            setResumeLoading(false);
        }
    }

    const currentPlan = subscriptionInfo?.plan ?? null;
    const cancellationScheduled = subscriptionInfo?.cancellationScheduled ?? false;
    const subscriptionEnd = subscriptionInfo?.subscriptionEnd ?? null;

    return (
        <div className="subscriptionsPage">
            <div className="subscriptionsHeader">
                <h1>Subscriptions</h1>
                <p>Choose the best plan for your account.</p>
            </div>

            {message && (
                <p className={`subscriptionMessage ${messageType}`}>
                    {message}
                </p>
            )}

            {cancellationScheduled && subscriptionEnd && (
                <p className="subscriptionMessage success">
                    Your subscription is scheduled to end on {formatSubscriptionEnd(subscriptionEnd)}.
                </p>
            )}

            <div className="plansGrid">
                <div className="planCard regularPlan">
                    <div className="planBadge">Free</div>
                    <h2>Regular</h2>
                    <h3>0 zł</h3>

                    <ul>
                        <li>Basic URL management</li>
                        <li>Standard dashboard access</li>
                        <li>Good for casual users</li>
                    </ul>

                    <button className="planButton regularButton" disabled>
                        {loadingSubscription
                            ? "Loading..."
                            : currentPlan === "REGULAR"
                                ? "Current plan"
                                : "Default plan"}
                    </button>
                </div>

                <div className="planCard premiumPlan">
                    <div className="planBadge premiumBadge">Most Popular</div>
                    <h2>Premium</h2>
                    <h3>9.99 zł</h3>

                    <ul>
                        <li>More URLs available</li>
                        <li>Better account limits</li>
                        <li>Designed for active users</li>
                    </ul>

                    <button
                        className="planButton premiumButton"
                        onClick={handleBuyPremium}
                        disabled={
                            loadingPremium ||
                            loadingSubscription ||
                            currentPlan === "PREMIUM"
                        }
                    >
                        {loadingSubscription
                            ? "Loading..."
                            : currentPlan === "PREMIUM"
                                ? "Current plan"
                                : loadingPremium
                                    ? "Loading..."
                                    : "Buy Premium"}
                    </button>
                </div>
            </div>

            <div className="cancelSection">
                <h2>Manage current subscription</h2>
                <p>
                    {cancellationScheduled && subscriptionEnd
                        ? `Your subscription has already been cancelled and will expire on ${formatSubscriptionEnd(subscriptionEnd)}.`
                        : "You can cancel your active subscription at period end."}
                </p>

                <button
                    className="cancelSubscriptionBtn"
                    onClick={
                        cancellationScheduled
                            ? handleResumeSubscription
                            : handleCancelSubscription
                    }
                    disabled={
                        cancelLoading ||
                        resumeLoading ||
                        loadingSubscription ||
                        currentPlan !== "PREMIUM"
                    }
                >
                    {cancelLoading
                        ? "Cancelling..."
                        : resumeLoading
                            ? "Resuming..."
                            : cancellationScheduled
                                ? "Resume subscription"
                                : "Cancel subscription"}
                </button>
            </div>
        </div>
    );
}

export default Subscriptions;