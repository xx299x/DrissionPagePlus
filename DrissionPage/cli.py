import click
from DrissionPage.easy_set import set_paths


@click.command()
@click.option("-p", "--browser-path", help="Setting browser path.")
def main(browser_path):
    """DrissionPage CLI."""
    if browser_path:
        set_paths(browser_path=browser_path)
        return 0


if __name__ == '__main__':
    main()
