from playwright.sync_api import sync_playwright

def verify_dashboard():
    with sync_playwright() as p:
        browser = p.chromium.launch(headless=True)
        page = browser.new_page()
        page.set_viewport_size({"width": 1366, "height": 768})

        try:
            # Go to the local dev server
            page.goto("http://localhost:5173")

            # 1. Upload Mode Check
            page.wait_for_selector("text=Upload Telegram Data")
            page.screenshot(path="verification/1_upload_mode.png")
            print("Captured upload mode.")

            # 2. Switch to Dashboard Mode
            page.click("button:has-text('Select File')")

            # Wait for dashboard to load (simulated delay is 1s)
            page.wait_for_selector("text=Overview", timeout=5000)

            # 3. Dashboard Layout Check
            # Check for Left Sidebar
            page.wait_for_selector("text=Dashboard")

            # Check for Right Sidebar (User Details)
            page.wait_for_selector("text=Media Types")

            # Capture Dashboard
            page.screenshot(path="verification/2_dashboard_mode.png")
            print("Captured dashboard mode.")

            # 4. Interaction Check (Select another user)
            page.click("text=Bob Smith")
            page.wait_for_selector("text=Bob Smith", state="visible") # Wait for right sidebar to update

            page.screenshot(path="verification/3_user_selected.png")
            print("Captured user selection.")

        except Exception as e:
            print(f"Error: {e}")
        finally:
            browser.close()

if __name__ == "__main__":
    verify_dashboard()
