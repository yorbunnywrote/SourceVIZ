from playwright.sync_api import sync_playwright

def verify_dashboard_update():
    with sync_playwright() as p:
        browser = p.chromium.launch(headless=True)
        page = browser.new_page()
        page.set_viewport_size({"width": 1366, "height": 768})

        try:
            # Go to the local dev server
            page.goto("http://localhost:5173")

            # Wait for dashboard to load (simulated delay is 1s, but we force it now by bypassing upload if needed)
            # Since mockData default is Upload Mode again? No, I left it true in previous step, let's check App.jsx.
            # I changed it to true then back to false? No, I changed it to false (upload mode) then back to true (dashboard mode) in the previous turn.
            # So it should be in dashboard mode.

            page.wait_for_selector("text=Overview", timeout=10000)

            # 1. Check Left Sidebar Changes
            # Search / N-grams buttons
            page.wait_for_selector("text=Search")
            page.wait_for_selector("text=N-grams")

            # Click N-grams to verify toggle
            page.click("text=N-grams")
            page.wait_for_selector("text=N-Count")
            page.wait_for_selector("text=Top K")
            page.screenshot(path="verification/4_left_sidebar_ngram.png")
            print("Captured Left Sidebar N-gram mode.")

            # 2. Check Center Stage Changes
            # Filter Bar
            page.wait_for_selector("text=Apply Filters")
            page.wait_for_selector("select") # Session dropdown

            # Users Table Columns
            # We expect "Msgs", "Words", "% Msgs", "% Words"
            page.wait_for_selector("text=Msgs")
            page.wait_for_selector("text=Words")
            page.wait_for_selector("text=% Msgs")
            page.wait_for_selector("text=% Words")

            # Capture Center Stage
            page.screenshot(path="verification/5_center_refactor.png")
            print("Captured Center Stage Refactor.")

        except Exception as e:
            print(f"Error: {e}")
        finally:
            browser.close()

if __name__ == "__main__":
    verify_dashboard_update()
