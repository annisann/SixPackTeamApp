import requests
import pandas as pd
from tqdm import tqdm
from bs4 import BeautifulSoup

def get_url(query, page):
    """
    Generate URL from query and page number.
    """
    url = 'https://www.lapor.go.id/search?q={}&page={}'.format(query, page)
    return url
  
def get_html_source(url):
    """
    Send request and log in to the website and return the HTML code.
    Replace value of the code below with yours.
    See README for more information.
    """
    cookies = {
        'lapor': 'eyJpdiI6Ing0MVlmM0RoU1lwSG9ldnA5S2VKS3c9PSIsInZhbHVlIjoiYVlEYUZJMHQyKzV3UnRUSHdiRjVOcGpjZ0YyeWk0WjJzazBmRlo1T1BSSjlGaXRnOFwveEdYRFRRMDREN3dqYUltMUw4cU9FSWRPc0E0MlFFa01SSEJ3PT0iLCJtYWMiOiJiNGJjODhhMzU1OGI0MmQ1NjA2ZGFlYjE3Njc0ZTVhZDEwMmFiYWY2NzJjNmIyZTZiMjkxYzRlNDI3NTNmOWNmIn0%3D',
    }

    headers = {
        'User-Agent': 'Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:88.0) Gecko/20100101 Firefox/88.0',
        'Accept': '*/*',
        'Accept-Language': 'en-US,en;q=0.5',
        'Content-Type': 'application/x-www-form-urlencoded; charset=UTF-8',
        'X-OCTOBER-REQUEST-HANDLER': 'laporAuth::onSignin',
        'X-OCTOBER-REQUEST-PARTIALS': 'captcha-login',
        'X-OCTOBER-REQUEST-FLASH': '1',
        'X-Requested-With': 'XMLHttpRequest',
        'Origin': 'https://www.lapor.go.id',
        'Connection': 'keep-alive',
        'Referer': 'https://www.lapor.go.id/',
        'Sec-GPC': '1',
    }

    data = {
      '_session_key': 'YOUR_SESSION_KEY',
      '_token': 'YOUR_TOKEN',
      'login': 'YOUR_EMAIL',
      'password': 'YOUR_PASSWORD'
    }

    try:
        response = requests.get(url, headers=headers, cookies=cookies, timeout=60)
        html_code = BeautifulSoup(response.text, 'html.parser')
        response = requests.post(url, headers=headers, cookies=cookies, data=data)
        return html_code
    
    except Exception:
        print('  ERROR: Timeout')
  
def get_report(query, page_len):
    """
    Extract user's reports from the website based on query and page number.
    """
    url = get_url(query, page_len)

    print('   + Getting HTML source . .')
    html = get_html_source(url)

    pageExist = html.find('p', {'class':'readmore'})
    if pageExist:
        reports = [(report.text, unit.text) for report, unit in zip(html.find_all('p', {'class':'readmore'}), html.find_all('b'))]
        return reports
    # for non existing page i.e no reports.
    elif pageExist==False:
        raise Exception('  ERROR: Page is not exist! Going to the next page ..')
        pass
  
def generate_dataframe(reports):
    """
    Generate a DataFrame from list of tuples.
    The reports output is [(report1, unit1), (report2, unit2), (report3, unit3), ...].
    In that case, for each report1, report2, report3 will be added to dictionary, as well as unit1, unit2, unit3.
    Then, DataFrame will be created from the dictionary.
    """
    reportDict = {'report': [],
                  'unit': []}

    for report, unit in reports:
        reportDict['report'].append(report)
        reportDict['unit'].append(unit)
    return pd.DataFrame.from_dict(reportDict)

def writeFile(dataframe):
    """
    Save the DataFrame into a csv file.
    """
    with open('{}.csv'.format(query), 'a+') as file:
        dataframe.to_csv(file, header=True)
  
def main():   
    global query, page_len
    global df
    
    query, page_len = input(), str(input())

    for i in range(1, page_len+1):
        print('Scraping page {}...'.format(i))
        report = get_report(query, page_len)
        df = generate_dataframe(report)
        writeFile(df)
    
main()
